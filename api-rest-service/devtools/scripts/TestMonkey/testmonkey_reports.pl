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
#my $BASE_URI = "http://localhost:8080/v1";
# NOTE: To go against PHINICS-DEV server, replace BASE_URI and call usBasicAuthorizarion().
my $BASE_URI = "http://129.55.210.57/papi-svc/v1";



#
# Report resource tests.
#

sub testPostResourceRequest
{
	my ($seconds, $minutes, $hour, $dayofmonth, $month, $year, $dayofweek, $dayofyear, $dst)  = localtime();
	my $serial = "-".$hour.$minutes.$seconds;
	
	my ($senderUserId, $incidentId, $userName) = @_;
	my $seqTime = time * 1000;
	my $msgText = '"{\"resreq-quantity\":\"5\",\"resreq-priority\":\"U\",\"resreq-description\":\"description\",\"resreq-type\":\"O\",\"resreq-source\":\"Some source\",\"resreq-location\":\"The beach\",\"resreq-eta\":\"4 Hours\",\"resreq-status\":\"Filled\",\"reporttm\":\"2013-09-09 16:12:03\"' .
		',\"user\":\"' . $userName . '\"' . '}"';
	print "Posting message>> " . $msgText . "\n";
	my $payload = '{"incidentId":' . $incidentId . ',"message":' . $msgText . ',"senderUserId":' . $senderUserId .
		',"seqNum":0,"seqTime":' . $seqTime .
		'}';
	print "POST PAYLOAD:> " . $payload . "\n";
	my $res = doJSONPost($BASE_URI . "/reports/RESREQ", $payload);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
}

sub testGetResourceRequest
{
	my $userName = shift;
	my $res = doJSONGet($BASE_URI . "/reports/RESREQ?user=" . $userName);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
}


#
# Main
#
my $incidentId = 11;
my $senderUserId = 7779;
my $userName = 'santiago.paredes@ll.mit.edu';

# NOTE: To go against PHINICS-DEV server, replace BASE_URI up top and call usBasicAuthorizarion().
useBasicAuthorization(1);
testGetResourceRequest($userName);
testPostResourceRequest($senderUserId, $incidentId, $userName);
testGetResourceRequest($userName);

