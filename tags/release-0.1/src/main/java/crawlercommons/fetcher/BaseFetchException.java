/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package crawlercommons.fetcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

@SuppressWarnings({ "serial" })
public abstract class BaseFetchException extends Exception {
    private String _url = "";
    private Exception _exception;
    
	protected BaseFetchException() {
		super();
		
        _exception = new Exception();
    }
    
    protected BaseFetchException(String url) {
    	super();
    	
        _exception = new Exception();
        _url = url;
    }
    
    protected BaseFetchException(String url, String msg) {
    	super(msg);
    	
        _exception = new Exception(msg);
        _url = url;
    }
    
    protected BaseFetchException(String url, Exception e) {
    	super(e);
    	
        _exception = new Exception(e);
        _url = url;
    }
    
    protected BaseFetchException(String url, String msg, Exception e) {
    	super(msg, e);
    	
        _exception = new Exception(msg, e);
        _url = url;
    }
    
    // Our specific methods
    public String getUrl() {
        return _url;
    }
    
    protected int compareToBase(BaseFetchException e) {
        return _url.compareTo(e._url);
    }

    @Override
	public boolean equals(Object obj) {
		return _exception.equals(obj);
	}

	@Override
	public Throwable getCause() {
		return _exception.getCause();
	}

	@Override
	public String getLocalizedMessage() {
		return _exception.getLocalizedMessage();
	}

	@Override
	public String getMessage() {
		return _exception.getMessage();
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		return _exception.getStackTrace();
	}

	@Override
	public int hashCode() {
		return _exception.hashCode();
	}

	@Override
	public Throwable initCause(Throwable cause) {
		return _exception.initCause(cause);
	}

	@Override
	public void printStackTrace() {
		_exception.printStackTrace();
	}

	@Override
	public void printStackTrace(PrintStream s) {
		_exception.printStackTrace(s);
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		_exception.printStackTrace(s);
	}

	@Override
	public void setStackTrace(StackTraceElement[] stackTrace) {
		_exception.setStackTrace(stackTrace);
	}

	@Override
	public String toString() {
		return _url + ": " + _exception.toString();
	}

    protected void readBaseFields(DataInput input) throws IOException {
    	int serializedLen = input.readInt();
    	byte[] serialized = new byte[serializedLen];
    	input.readFully(serialized);
    	ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serialized));
    	
    	try {
    		_exception = (Exception)ois.readObject();
    	} catch (ClassNotFoundException e) {
    		throw new IOException(e);
    	}
    	
        _url = input.readUTF();
    }
    
    protected void writeBaseFields(DataOutput output) throws IOException {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ObjectOutputStream oos = new ObjectOutputStream(bos);
    	oos.writeObject(_exception);
    	byte[] serialized = bos.toByteArray();
    	output.writeInt(serialized.length);
    	output.write(bos.toByteArray());
        output.writeUTF(_url);
    }
    
}
