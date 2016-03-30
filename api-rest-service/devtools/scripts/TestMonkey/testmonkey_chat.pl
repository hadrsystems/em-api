#!/usr/bin/perl
#
# Copyright (c) 2008-2016, Massachusetts Institute of Technology (MIT)
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

require 'testmonkey_common.pl';

my $CURL = 'curl';
my $JSON_HDRS = '-H "Accept: application/json" -H "Content-type: application/json; charset=UTF-8"';
my $BASE_URI = "http://localhost:8080/v1";
# NOTE: To go against PHINICS-DEV server, replace BASE_URI and call usBasicAuthorizarion().
#my $BASE_URI = "http://129.55.210.57/papi-svc/v1";



#
# Chat resource tests.
#

sub testPostChat
{
	my ($seconds, $minutes, $hour, $dayofmonth, $month, $year, $dayofweek, $dayofyear, $dst)  = localtime();
	my $serial = "-".$hour.$minutes.$seconds;
	
	my ($incidentName, $collabRoomId, $senderId) = @_;
	my $topic = 'LDDRS.incidents.' . $incidentName . '.collab.IncidentMap';
	my $msgText = "Test Message from mobile UserId " . $senderId . " to Incident " . $incidentName
		. " CollabRoomId " . $collabRoomId . "  on " . ($year + 1900) . "/" . ($month + 1)
		. "/$dayofmonth $hour:$minutes:$seconds";
	my $seqTime = time * 1000;
	my $sessionId = 389;
	
	print "Posting message>> " . $msgText . "\n";
	my $res = doJSONPost($BASE_URI . "/chatmsgs/" . $topic,
		'{"collabRoomId":' . $collabRoomId . ',"msgText":"' . $msgText . '","senderUserId":' . $senderId .
		',"seqNum":0,"seqTime":' . $seqTime .
		'}'
		);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
}

sub testGetChat
{
	my $incidentName = shift;
	my $topic = 'LDDRS.incidents.' . $incidentName . '.collab.IncidentMap';
	my $res = doJSONGet($BASE_URI . "/chatmsgs/" . $topic);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
}


#
# Main
#

my $useSectorNOLA=0;
my $incidentName = undef;
my $collabRoomId = undef;
my $senderId = undef;

if ($useSectorNOLA) {
	$incidentName = "SectorNOLA";
	$collabRoomId = 21;
	$senderId = 5;
} else {
	$incidentName = "MAMITLLSanti02";
	$collabRoomId = 11;
	$senderId = 5;	
}

# NOTE: To go against PHINICS-DEV server, replace BASE_URI up top and call usBasicAuthorizarion().
#useBasicAuthorization(1);
testPostChat($incidentName, $collabRoomId, $senderId);
testGetChat($incidentName);

