/**
 * Copyright (c) 2008-2018, Massachusetts Institute of Technology (MIT)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.mit.ll.em.api.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Any address in the range 127.xxx.xxx.xxx is a "loopback" address. It is only
 * visible to "this" host. Any address in the range 192.168.xxx.xxx is a
 * private IP address. These are reserved for use within an organization. The
 * same applies to 10.xxx.xxx.xxx addresses, and 172.16.xxx.xxx through
 * 172.31.xxx.xxx. Addresses in the range 224.xxx.xxx.xxx through
 * 239.xxx.xxx.xxx are multicast addresses.
 * The address 255.255.255.255 is the broadcast address.
 * Anything else should be valid public point-to-point IPv4 address.
 * @author SA23148
 *
 */
public class NetUtil {
	public static final String LOOPBACK_ADDR = "127.0.0.1";
	private Set<String> myExternalIPAddrs = new HashSet<String>();

	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static NetUtil instance = new NetUtil();
	}

	public static  NetUtil getInstance() {
		return Holder.instance;
	}
	
	public Set<String> getExternalAddresses() {
		return Collections.unmodifiableSet(myExternalIPAddrs);
	}

	protected NetUtil() {
		loadHostIpAddresses();
	}

	private void loadHostIpAddresses() {
		@SuppressWarnings("rawtypes")
		Enumeration e = null; 
		try {
			e = NetworkInterface.getNetworkInterfaces();
			while(e.hasMoreElements())
			{
				NetworkInterface n=(NetworkInterface) e.nextElement();
				@SuppressWarnings("rawtypes")
				Enumeration ee = n.getInetAddresses();
				while(ee.hasMoreElements())
				{
					InetAddress i = (InetAddress) ee.nextElement();
					String hostAddr = i.getHostAddress();
					if (hostAddr.startsWith("127.") ||
						hostAddr.startsWith("192.168.") ||
						hostAddr.startsWith("0:")) {
						continue;
					}
					myExternalIPAddrs.add(hostAddr);
				}
			}			
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
