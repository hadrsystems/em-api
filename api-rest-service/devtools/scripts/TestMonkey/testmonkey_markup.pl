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
my $JSON_HDRS = '-H "Accept: application/json" -H "Content-type: application/json; charset=UTF-8"';
# NOTE: To go against PHINICS-DEV server, replace BASE_URI and call usBasicAuthorizarion().
my $BASE_URI = "http://localhost:8080/v1";
#my $BASE_URI = "http://129.55.210.57/papi-svc/v1";



#
# Chat resource tests.
#

sub testPostMarkup2
{
	my ($seconds, $minutes, $hour, $dayofmonth, $month, $year, $dayofweek, $dayofyear, $dst)  = localtime();
	my $serial = "-".$hour.$minutes.$seconds;
	
	my ($incidentId, $collabRoomId, $senderId) = @_;

	my $seqTime = time * 1000;
	my $sessionId = 389;
			
	my $box = '{"features":['
			. '{"dashStyle":"solid","fillColor":"#FF0000","isGesture":false,"strokeColor":"#FF0000",'
			. '"strokeWidth":2.0,"type":"box","points":[[-7438319.0975028,5524224.6815004],'
			. '[-7440765.0824076,5463075.0588808],[-7379615.459788,5460629.073976],[-7377169.4748832,5521778.6965956],'
			. '[-7438319.0975028,5524224.6815004]],"radius":2.0}]' 
			. ',"collabRoomId":'
			. $collabRoomId . ',"seqTime":1373550649653,"senderUserId":' . $senderId
			. ',"incidentId":' . $incidentId . '}';
	print "BOX-FORMAT EXAMPLE> " . $box . "\n";
		my $res = doJSONPost($BASE_URI . "/mapmarkups/", $box);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
	
	return;
			
	my $line = '{"features":['
			. '{"dashStyle":"solid","fillColor":"#DC0000","isGesture":false,"strokeColor":"#DC0000",'
			. '"strokeWidth":2.0,"type":"line","points":[[-7438000.0975028,5524333.6815004],'
			. '[-7379615.459788,5460629.073976]],"radius":2.0}]' 
			. ',"collabRoomId":'
			. $collabRoomId . ',"seqTime":1373550649654,"senderUserId":' . $senderId
			. ',"incidentId":' . $incidentId . '}';
	print "LINE-FORMAT EXAMPLE> " . $line . "\n";			
	
	$res = doJSONPost($BASE_URI . "/mapmarkups/", $line);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);				
			
	my $circle = '{"features":['
			. '{"dashStyle":"solid","fillColor":"#BA0000","isGesture":false,"strokeColor":"#BA0000",'
			. '"strokeWidth":2.0,"type":"circle","points":[[-7438319.0975028,5524224.6815004]],"radius":100000.0}]' 
			. ',"collabRoomId":'
			. $collabRoomId . ',"seqTime":1373550649654,"senderUserId":' . $senderId
			. ',"incidentId":' . $incidentId . '}';
	print "CIRCLE-FORMAT EXAMPLE> " . $circle . "\n";
	
	$res = doJSONPost($BASE_URI . "/mapmarkups/", $circle);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
}


sub testPostMarkup
{
	my ($seconds, $minutes, $hour, $dayofmonth, $month, $year, $dayofweek, $dayofyear, $dst)  = localtime();
	my $serial = "-".$hour.$minutes.$seconds;
	
	my ($incidentId, $collabRoomId, $senderId) = @_;

	my $seqTime = time * 1000;
	my $sessionId = 389;
	
	my $box = '{"features":['
			. '{"dashStyle":"solid","fillColor":"#FF0000","isGesture":false,"strokeColor":"#FF0000",'
			. '"strokeWidth":2.0,"type":"box","points":[[42.51238831863514,-71.0932389744789],'
			. '[42.9964005814337,-71.0932389744789],[42.9964005814337,-71.75241866188752],[42.51238831863514,-71.75241866188752],'
			. '[42.51238831863514,-71.0932389744789]],"radius":2.0}]'
			. ',"collabRoomId":'
			. $collabRoomId . ',"seqTime":1373550649653,"senderUserId":' . $senderId
			. ',"incidentId":' . $incidentId . '}';
	print "BOX-FORMAT EXAMPLE> " . $box . "\n";
		my $res = doJSONPost($BASE_URI . "/mapmarkups/", $box);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
			
	my $line = '{"features":['
			. '{"dashStyle":"solid","fillColor":"#DC0000","isGesture":false,"strokeColor":"#DC0000",'
			. '"strokeWidth":2.0,"type":"line","points":[[42.51238831863514,-71.0932389744789],'
			. '[41.8964005814337,-70.65241866188752]],"radius":2.0}]' 
			. ',"collabRoomId":'
			. $collabRoomId . ',"seqTime":1373550649654,"senderUserId":' . $senderId
			. ',"incidentId":' . $incidentId . '}';
	print "LINE-FORMAT EXAMPLE> " . $line . "\n";			
	
	$res = doJSONPost($BASE_URI . "/mapmarkups/", $line);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);				
			
	my $circle = '{"features":['
			. '{"dashStyle":"solid","fillColor":"#BA0000","isGesture":false,"strokeColor":"#BA0000",'
			. '"strokeWidth":2.0,"type":"circle","points":[[41.61238831863514,-70.1932389744789]],"radius":0.5}]' 
			. ',"collabRoomId":'
			. $collabRoomId . ',"seqTime":1373550649654,"senderUserId":' . $senderId
			. ',"incidentId":' . $incidentId . '}';
	print "CIRCLE-FORMAT EXAMPLE> " . $circle . "\n";
	
	$res = doJSONPost($BASE_URI . "/mapmarkups/", $circle);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
}


sub testPostCircle
{
	my ($seconds, $minutes, $hour, $dayofmonth, $month, $year, $dayofweek, $dayofyear, $dst)  = localtime();
	my $serial = "-".$hour.$minutes.$seconds;
	
	my ($incidentId, $collabRoomId, $senderId) = @_;

	my $seqTime = time * 1000;
	my $sessionId = 389;
			
	#my $circle = '{"features":['
    #		. '{"dashStyle":"solid","fillColor":"#BA0000","isGesture":false,"strokeColor":"#BA0000",'
	#		. '"strokeWidth":2.0,"type":"circle","points":[[41.61238831863514,-70.1932389744789]],"radius":0.5}]' 
	#		. ',"collabRoomId":'
	#		. $collabRoomId . ',"seqTime":1373550649654,"senderUserId":' . $senderId
	#		. ',"incidentId":' . $incidentId . '}';
	
	my $circle = '{"features":[{"type":"circle","dashStyle":"solid","fillColor":"#00FFFF","graphic":"","topic":"LDDRS.incidents.MAMITLLoilspill.collab.IncidentMap","strokeColor":"#00FFFF","points":[[42.3596979262089,-70.99747952073812]],"ipAddr":"127.0.0.1","opacity":0.2,"nickname":"Piyush Agarwal","radius":1000.0,"rotation":0.0,"labelSize":0.0,"seqNum":0,"seqTime":1391029826384,"graphicWidth":0.0,"strokeWidth":2.0,"graphicHeight":0.0,"isGesture":false}],"createdUTC":1391029826629,"incidentId":'
			. $incidentId .',"lastUpdatedUTC":1391029826629,"collabRoomId":'
			. $collabRoomId . ',"senderUserId":' . $senderId . ',"seqTime":1391029826629}';
	print "CIRCLE-FORMAT EXAMPLE> " . $circle . "\n";
	
	my $res = doJSONPost($BASE_URI . "/mapmarkups/1/", $circle);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
}


sub testGetMarkup
{
	my ($workspaceId, $collabRoomId) = @_;
	my $res = doJSONGet($BASE_URI . "/mapmarkups/" . $workspaceId . "/" . $collabRoomId);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
}

sub testDeleteMarkup
{
	my ($featureId) = @_;
	my $res = doJSONDelete($BASE_URI . "/mapmarkups/" . $featureId);
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
}

#
# Main
#
my $useSectorNOLA=0;
my $incidentId = undef;
my $collabRoomId = undef;
my $senderId = undef;
my $workspaceId = 1;

if ($useSectorNOLA) {
	$incidentId = 11;
	$collabRoomId = 21;
	$senderId = 5;
} else {
	#$incidentId = 8;
	#$collabRoomId = 11;
	#$senderId = 5;
	$incidentId = 45;
	$collabRoomId = 165;
	$senderId = 5;		
}

# NOTE: To go against PHINICS-DEV server, replace BASE_URI up top and call usBasicAuthorizarion().
#useBasicAuthorization(1);
#print "Calling POST MapMarkups...\n";
#testPostMarkup($incidentId, $collabRoomId, $senderId);
#testPostCircle($incidentId, $collabRoomId, $senderId);
print "Calling GET MapMarkups...\n";
testGetMarkup($workspaceId, $collabRoomId);

#my $featureId = '7B8A6E73-257F-4BB3-AF95-07F4A8C0B823';
#testDeleteMarkup($featureId);

