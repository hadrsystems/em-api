#!/usr/bin/perl
#
# Copyright (c) 2008-2015, Massachusetts Institute of Technology (MIT)
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice, this
# list of conditions and the following disclaimer.
#
# 2. Redistributions in binary form must reproduce the above copyright notice,
# this list of conditions and the following disclaimer in the documentation
# and/or other materials provided with the distribution.
#
# 3. Neither the name of the copyright holder nor the names of its contributors
# may be used to endorse or promote products derived from this software without
# specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

# A PAPI Test Driver.

use strict;
use warnings;
use FileHandle;
use LWP::Simple;

my $CURL      = 'curl';
my $BASE_URI  = "http://129.55.210.57/papi-svc/v1";
my $JSON_HDRS =
'-H "Accept: application/json" -H "Content-type: application/json; charset=UTF-8"';

#
# Some primitives
#

sub doJSONOp {
	my $op   = shift;
	my $url  = shift;
	my $json = shift;
	my $req  = HTTP::Request->new( $op => $url );
	$req->content_type('application/json');
	$req->content($json);

	my $ua  = LWP::UserAgent->new;    # You might want some options here
	my $res = $ua->request($req);
	return $res;
}

sub doJSONDelete {
	my $url  = shift;
	my $json = shift;
	return doJSONOp( 'DELETE', $url, $json );
}

sub doJSONGet {
	my $url  = shift;
	my $json = shift;
	return doJSONOp( 'GET', $url, $json );
}

sub doJSONPut {
	my $url  = shift;
	my $json = shift;
	return doJSONOp( 'PUT', $url, $json );
}

sub doJSONPost {
	my $url  = shift;
	my $json = shift;
	return doJSONOp( 'POST', $url, $json );
}

#
# User resource tests.
#
sub testAddUser {
	my $res = doJSONPost( $BASE_URI . "/users",
		'{"userId":null,"firstName":"Teofilo","lastName":"Cubillas"}' );
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ( $res->is_success && $res->content =~ /"message":"ok"/ );
	my $userId = undef;
	if ( $res->content =~ m/\"userId\":(\d+)/g ) {
		$userId = $1;
	}
	return $userId;
}

sub testGetUser {
	my $id  = shift;
	my $res = doJSONGet( $BASE_URI . "/users/" . $id );
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ( $res->is_success && $res->content =~ /"message":"ok"/ );
	my $userId = undef;
	if ( $res->content =~ m/\"userId\":(\d+)/g ) {
		$userId = $1;
	}
	die unless $userId == $id;
}

sub testRemoveUser {
	my $userId = shift;
	my $res    = doJSONDelete( $BASE_URI . "/users/" . $userId );
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ( $res->is_success && $res->content =~ /"message":"ok"/ );
}

sub testUpdateUser {
	my $id  = shift;
	my $res = doJSONPut( $BASE_URI . "/users/" . $id,
		'{"userId":null,"firstName":"Hugo","lastName":"Sotil"}' );
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ( $res->is_success && $res->content =~ /"message":"ok"/ );
	my $userId = undef;
	if ( $res->content =~ m/\"userId\":(\d+)/g ) {
		$userId = $1;
	}
	die unless ( $res->is_success && $res->content =~ /"message":"ok"/ );
	die unless $userId == $id;
	die unless $res->content =~ /Sotil/;
}

#
# MsgBus tests.
#
sub testMsgBusGet {
	my $res = doJSONGet( $BASE_URI . "/msgbus/12345" );
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ( $res->is_success && $res->content =~ /"message":"ok"/ );
	die unless ( $res->content =~ /Great to see you!!!/ );
}

sub testMsgBusPost {
	my $res = doJSONPost(
		$BASE_URI . "/msgbus/12345",
'[{"msgType":"SampleBean","msgPayload":"{\"count\":7,\"msg\":\"Great to see you!!!\"}"}]'
	);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ( $res->is_success && $res->content =~ /"message":"ok"/ );
}

sub testMsgBusSubscribe {
	my $res = doJSONPost( $BASE_URI . "/msgbus",
		'{"TOPICS":"t1+t2","TIMEOUT":"1000","SUBSCRIBERID":"12345"}' );
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ( $res->is_success && $res->content =~ /"message":"ok"/ );
}

sub testMsgBusUnsubscribe {
	my $res = doJSONDelete( $BASE_URI . "/msgbus/12345" );
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ( $res->is_success && $res->content =~ /"message":"ok"/ );
}

sub getAllUsers {
	my $payload  = shift;
	my $browser  = LWP::UserAgent->new();
	my $response = $browser->get("$BASE_URI/users");
	return $response;
}

#
# Main
#
my $userId = testAddUser();
testGetUser($userId);
testUpdateUser($userId);
testRemoveUser($userId);

testMsgBusSubscribe();
testMsgBusPost();
testMsgBusGet();
testMsgBusUnsubscribe();
