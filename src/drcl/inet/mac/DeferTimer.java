// @(#)DeferTimer.java   7/2003
// Copyright (c) 1998-2003, Distributed Real-time Computing Lab (DRCL) 
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 
// 1. Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer. 
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution. 
// 3. Neither the name of "DRCL" nor the names of its contributors may be used
//    to endorse or promote products derived from this software without specific
//    prior written permission. 
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// 

package drcl.inet.mac;


/**
 * The class implements some functions of DeferTimer of IEEE 802.11 protocol.
 * This class is ported from ns-2.1b7a.
 *
 * @see Mac_802_11
 * @see Mac_802_11_Timer
 * @author Ye Ge
 */
class DeferTimer extends Mac_802_11_Timer {

    /**
     * Contructor.
     */
	public DeferTimer(Mac_802_11 h, double s) {
		super(h, s);
		o_.setType(MacTimeoutEvt.Defer_timeout); 
	}	

    /**
     * Contructor.
     */
	public DeferTimer(Mac_802_11 h) {
		super(h, 0);
		o_.setType(MacTimeoutEvt.Defer_timeout); 
	}	

    /**
     * Handles the timeout event. This method is called in Mac_802_11 class.
     */
	public void handle( ) {       
		busy_ = false;
		paused_ = false;
		stime = 0.0;
		rtime = 0.0;
    }

    /**
     * Starts the timer.
     */
    public void start(double time) {
        _assert("DeferTimer start()", "busy_ == false", (busy_ == false));
     	busy_ = true;
		paused_ = false;
		stime = host_.getTime();
		rtime = time;

        _assert("DeferTimer start()", "rtime >= 0.0", (rtime >= 0.0));
		host_.setTimeout(o_, rtime);
	}
}

