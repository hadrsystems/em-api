#!/usr/bin/perl
#
# Copyright (c) 2008-2018, Massachusetts Institute of Technology (MIT)
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

my $username = 'santiago.paredes@ll.mit.edu';
my $pass = "!asecret";
my $useBasicAuthorization = undef;


sub useBasicAuthorization
{
	$useBasicAuthorization = shift;
}

#
# Some primitives
#
sub doJSONOp
{	
	my $op = shift;
	my $url = shift;
	my $json = shift;
	my $req = HTTP::Request->new($op => $url);
	$req->content_type('application/json');
	$req->content($json);
	$req->authorization_basic( "$username", "$pass" )
		if defined($useBasicAuthorization);

	my $ua = LWP::UserAgent->new; # You might want some options here
	my $res = $ua->request($req);
	return $res;			
}

sub doJSONDelete
{
	my $url = shift;
	my $json = shift;
	return doJSONOp('DELETE', $url, $json);	
}

sub doJSONGet
{
	my $url = shift;
	my $json = shift;
	return doJSONOp('GET', $url, $json);
}

sub doJSONPut
{
	my $url = shift;
	my $json = shift;
	return doJSONOp('PUT', $url, $json);
}

sub doJSONPost
{
	my $url = shift;
	my $json = shift;
	return doJSONOp('POST', $url, $json);
}
1;