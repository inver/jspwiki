/*
    JSPWiki - a JSP-based WikiWiki clone.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.    
 */
package com.ecyrd.jspwiki.dav;

import java.util.Properties;

import com.ecyrd.jspwiki.TestEngine;
import com.ecyrd.jspwiki.attachment.Attachment;
import com.ecyrd.jspwiki.dav.items.DavItem;
import com.ecyrd.jspwiki.dav.items.DirectoryItem;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AttachmentDavProviderTest extends TestCase
{
    Properties props = new Properties();

    private TestEngine m_engine = null;

    AttachmentDavProvider m_provider;
    
    protected void setUp() throws Exception
    {
        props.load( TestEngine.findTestProperties() );

        m_engine = new TestEngine(props);

        m_provider = new AttachmentDavProvider(m_engine);
    }

    protected void tearDown() throws Exception
    {
        TestEngine.deleteAttachments( "TestPage" );
        TestEngine.deleteTestPage("TestPage");
        
        m_engine.shutdown();
    }

    public void testGetPageURL()
        throws Exception
    {
        m_engine.saveText("TestPage", "foobar");
        Attachment att = new Attachment(m_engine,"TestPage","deceit of the tribbles.txt");
        
        m_engine.getAttachmentManager().storeAttachment( att, m_engine.makeAttachmentFile() );
        
        DavItem di = m_provider.getItem( new DavPath("TestPage/deceit of the tribbles.txt") );
        
        assertNotNull( "No di", di );
        assertEquals("URL", "http://localhost/attach/TestPage/deceit+of+the+tribbles.txt", 
                     di.getHref() );
    }

    public void testDirURL()
        throws Exception
    {
        m_engine.saveText("TestPage", "foobar");
    
        DavItem di = m_provider.getItem( new DavPath("") );
    
        assertNotNull( "No di", di );
        assertTrue( "DI is of wrong type", di instanceof DirectoryItem );
        assertEquals("URL", "http://localhost/attach/", di.getHref() );
    }

    public void testDirURL2()
        throws Exception
    {
        m_engine.saveText("TestPage", "foobar");

        DavItem di = m_provider.getItem( new DavPath("TestPage/") );

        assertNotNull( "No di", di );
        assertTrue( "DI is of wrong type", di instanceof DirectoryItem );
        assertEquals("URL", "http://localhost/attach/TestPage/", di.getHref() );
    }

    public static Test suite()
    {
        return new TestSuite( RawPagesDavProviderTest.class );
    }


}
