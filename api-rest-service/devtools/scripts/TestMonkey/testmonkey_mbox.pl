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
my $BASE_URI = "http://localhost:8080/v1";
my $JSON_HDRS = '-H "Accept: application/json" -H "Content-type: application/json; charset=UTF-8"';

#
# MBox tests.
#
sub testMBoxGet
{
	my $resourceId = shift;
	my $res = doJSONGet($BASE_URI . "/mbox/$resourceId");
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
	#die unless ($res->content =~ /Great to see you!!!/);
}

#
# TaskingAssignment Service tests.
#
sub testTaskingAssignmentGet
{
	my $resourceId = shift;
	my $res = doJSONGet($BASE_URI . "/tasks/$resourceId");
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
	#die unless ($res->content =~ /Great to see you!!!/);
}

#
# Incident Service tests.
#
sub testIncidentNotificationGet
{
	my $resourceId = shift;
	my $res = doJSONGet($BASE_URI . "/incidents/$resourceId/notification");
	print "TM_RESPONSE> " . $res->content . "\n";
	die unless ($res->is_success && $res->content =~ /"message":"ok"/);
	#die unless ($res->content =~ /Great to see you!!!/);
}


#
# Main
#

testMBoxGet("7777");
testTaskingAssignmentGet("1007");
#testIncidentNotificationGet("99");
