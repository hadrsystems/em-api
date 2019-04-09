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

require 'testmonkey_common.pl';

my $CURL = 'curl';
#my $BASE_URI = "http://localhost:8080/v1";
my $BASE_URI = "http://129.55.210.57/papi-svc/v1";
my $JSON_HDRS = '-H "Accept: application/json" -H "Content-type: application/json; charset=UTF-8"';

#
# User resource tests.
#
sub testAddMDT
{
	print "testAddMDT - Begin\n";
	my $userId = shift;
	my ($seconds, $minutes, $hour, $dayofmonth, $month, $year, $dayofweek, $dayofyear, $dst)  = localtime();
	my $serial = "-".$hour.$minutes.$seconds;
	my $nowInMillis = time * 1000;
	my $res = doJSONPost($BASE_URI . "/mdtracks",
		'{"userId":' . $userId . ',"deviceId":"XYZ-DEVID-' . $userId . '","latitude":42.579535,"longitude":-71.374722,' .
		'"altitude":10,"course":45.898,"speed":0.35,"accuracy":15.7,"creationUTC":' . $nowInMillis . '}'
		);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
	my $mdtId = undef;
	if ($res->content=~ m/\"mdtId\":(\d+)/g) {
		$mdtId = $1;
	}
	print "testAddMDT - End\n";
	return $mdtId;
}

sub testBulkUploadMDT
{
	print "testBulkUploadMDT - Begin\n";
	my ($seconds, $minutes, $hour, $dayofmonth, $month, $year, $dayofweek, $dayofyear, $dst)  = localtime();
	my $serial = "-".$hour.$minutes.$seconds;
	my $res = doJSONPut($BASE_URI . "/mdtracks",
		'['.
		'{"userId":7777,"deviceId":"XYZ-DEVID-7777","latitude":56.3245,"longitude":-3.24567,' .
		'"altitude":10,"course":45.898,"speed":0.35,"accuracy":15.7,"creationUTC":1365281469345},'.
		'{"userId":7777,"deviceId":"XYZ-DEVID-7777","latitude":56.3255,"longitude":-3.24577,' .
		'"altitude":10,"course":45.898,"speed":0.35,"accuracy":15.7,"creationUTC":1365281469350},'.
		'{"userId":7777,"deviceId":"XYZ-DEVID-7777","latitude":56.3265,"longitude":-3.24587,' .
		'"altitude":10,"course":45.898,"speed":0.35,"accuracy":15.7,"creationUTC":1365281469355}'.
		']'
		);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
	print "testBulkUploadMDT - End\n";
}

sub testGetMDT
{
	print "testGetMDT - Begin\n";	
	my $id = shift;
	my $res = doJSONGet($BASE_URI . "/mdtracks/" . $id);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
	my $mdtId = undef;
	if ($res->content=~ m/\"mdtId\":(\d+)/g) {
		$mdtId = $1;
	}
	die unless $mdtId == $id;
	print "testGetMDT - End\n";
}


sub testRemoveMDT
{
	print "testRemoveMDT - Begin\n";
	if (0) {  ## No business case for this one.
		my $mdtId = shift;
		my $res = doJSONDelete($BASE_URI . "/mdtracks/" . $mdtId);
		print "TM_RESPONSE> " . $res->content . "\n";
		die unless ($res->is_success && $res->content =~ /"message":"ok"/);
	}
	print "testUpdateMDT - End\n";
}


sub testUpdateMDT
{
	print "testUpdateMDT - Begin\n";
	if (0) {   ## No business case for this one.
		my $id = shift;
		my $res = doJSONPut($BASE_URI . "/mdtracks/" . $id,
			'{"deviceId":"ABC-DEVID-7777"}');
		print "TM_RESPONSE> " . $res->content . "\n";
		die unless ($res->is_success && $res->content =~ /"message":"ok"/);
		my $mdtId = undef;
		if ($res->content=~ m/\"mdtId\":(\d+)/g) {
			$mdtId = $1;
		}
		die unless ($res->is_success && $res->content =~ /"message":"ok"/);
		die unless $mdtId == $id;
		die unless $res->content =~ /ABC-DEVID-7777/;
	}
	print "testUpdateMDT - End\n";
}


#
# Main
#
my $userId = 5;
useBasicAuthorization(1);

my $mdtId = testAddMDT($userId);
print "Created MDT with ID: " . $mdtId . "\n";
testGetMDT($mdtId);
#testBulkUploadMDT();
#testUpdateMDT($mdtId);
#testRemoveMDT($mdtId);
