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
my $BASE_URI = "http://localhost:8080/v1";
my $JSON_HDRS = '-H "Accept: application/json" -H "Content-type: application/json; charset=UTF-8"';

#
# User resource tests.
#
sub testAddIncident
{
	my ($seconds, $minutes, $hour, $dayofmonth, $month, $year, $dayofweek, $dayofyear, $dst)  = localtime();
	my $serial = "-".$hour.$minutes.$seconds;
	my $res = doJSONPost($BASE_URI . "/incidents",
		'{"userSessionId":0,"incidentName":"Incident' . $serial . '","latitude":-89.870975,' .
		'"longitude":30.159622,"createdUTC":1365281469345,"active":true,' .
		'"folder":" "}'
		);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
	my $incidentId = undef;
	if ($res->content=~ m/\"incidentId\":(\d+)/g) {
		$incidentId = $1;
	}
	return $incidentId;
}

sub testGetIncident
{
	my $id = shift;
	my $res = doJSONGet($BASE_URI . "/incidents/" . $id);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
	my $incidentId = undef;
	if ($res->content=~ m/\"incidentId\":(\d+)/g) {
		$incidentId = $1;
	}
	die unless $incidentId == $id;
}

sub testRemoveIncident
{
	my $incidentId = shift;
	my $res = doJSONDelete($BASE_URI . "/incidents/" . $incidentId);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
}

sub testUpdateIncident
{
	my $id = shift;
	my $res = doJSONPut($BASE_URI . "/incidents/" . $id,
		'{"latitude":-80.870975}');
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
	my $incidentId = undef;
	if ($res->content=~ m/\"incidentId\":(\d+)/g) {
		$incidentId = $1;
	}
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
	die unless $incidentId == $id;
	die unless $res->content =~ /-80.870975/;
}


#
# Main
#
#testGetIncident('0');
my $incidentId = testAddIncident();
testGetIncident($incidentId);
testUpdateIncident($incidentId);
testRemoveIncident($incidentId);	
