// @(#)QueueFIFO.java   9/2002
// Copyright (c) 1998-2002, Distributed Real-time Computing Lab (DRCL) 
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

package drcl.comp.queue;

import java.util.*;
import drcl.comp.*;
import drcl.data.*;
import drcl.util.queue.FIFOQueue;

public class QueueFIFO extends ActiveQueue
{
	FIFOQueue q = null;
	int capacity = Integer.MAX_VALUE;
										
	public QueueFIFO() { super(); }
	
	public QueueFIFO(String id_) { super(id_); }
	
	public void reset()
	{
		super.reset();
		if (q != null) q.reset();
	}
	
	public void duplicate(Object source_)
	{
		super.duplicate(source_);
		QueueFIFO that_ = (QueueFIFO)source_;
	}
	
	public String info(String prefix_)
	{ return prefix_ + "FIFO: " + super.info() + (q != null? q.info(): "Queue is empty.\n"); }
	
	/**
	 * Enqueues the object at the end of the queue
	 * @return the object being dropped due to the enqueue; null otherwise.
	 */
	public Object enqueue(Object obj_)
	{
		if (q == null) q = new FIFOQueue();
		if (isFull()) return obj_;
		q.enqueue(obj_); 
		return null;
	}
	
	/**
	 * Enqueues the object at the position specified.
	 * @return the object being dropped due to the enqueue; null otherwise.
	 */
	public Object enqueueAt(Object obj_, int pos_)
	{
		if (q == null) q = new FIFOQueue();
		return q.enqueueAt(pos_, 0.0/*dont care*/, obj_)? null: obj_;
	}
	
	/**
	 * Dequeues and returns the first object in the queue.
	 * @return the object dequeued; null if position is not valid.
	 */
	public Object dequeue()
	{
		if (q == null) return null;
		return q.dequeue();
	}
	
	/**
	 * Dequeues the object at the position specified.
	 * @return the object dequeued; null if position is not valid.
	 */
	public Object retrieveAt(int pos_)
	{
		if (q == null) return null;
		Object o_ = q.retrieveAt(pos_);
		q.remove(pos_);
		return o_;
	}
	
	/**
	 * Retrieves but not dequeue the object at the position specified.
	 * @return the object; null if position is not valid.
	 */
	public Object peekAt(int pos_)
	{ return  q == null? null: q.retrieveAt(pos_); }
	
	/** Return true if the queue is full.  */
	public boolean isFull()
	{ return q == null? false: q.getLength() == capacity; }
	
	/** Return true if the queue is empty.  */
	public boolean isEmpty()
	{ return q == null? false: q.isEmpty(); }
	
	/**
	 * Sets the capacity of the queue.
	 * @param capacity_ the new capacity.
	 * @param truncate_ set true to drop objects that are outside the new capacity.
	 */
	public void setCapacity(int capacity_)
	{ capacity = capacity_; }
	
	/** Returns the capacity of the queue. */
	public int getCapacity()
	{ return capacity; }
	
	/** Returns the current size of the queue. */
	public int getSize()
	{ return q == null? 0: q.getLength(); }
}
