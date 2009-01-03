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
package com.ecyrd.jspwiki.auth.login;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import net.sourceforge.stripes.mock.MockHttpServletRequest;

import junit.framework.TestCase;

import com.ecyrd.jspwiki.NoRequiredPropertyException;
import com.ecyrd.jspwiki.TestAuthorizer;
import com.ecyrd.jspwiki.TestEngine;
import com.ecyrd.jspwiki.auth.Authorizer;
import com.ecyrd.jspwiki.auth.WikiPrincipal;
import com.ecyrd.jspwiki.auth.authorize.Role;
import com.ecyrd.jspwiki.auth.user.UserDatabase;
import com.ecyrd.jspwiki.auth.user.XMLUserDatabase;

/**
 * @author Andrew R. Jaquith
 */
public class WebContainerLoginModuleTest extends TestCase
{
    Authorizer authorizer;

    UserDatabase db;

    Subject      subject;

    private TestEngine m_engine;

    public final void testLogin()
    {
        Principal principal = new WikiPrincipal( "Andrew Jaquith" );
        MockHttpServletRequest request = m_engine.newHttpRequest();
        request.setUserPrincipal( principal );
        try
        {
            // Test using Principal (WebContainerLoginModule succeeds)
            CallbackHandler handler = new WebContainerCallbackHandler( m_engine, request, authorizer );
            LoginModule module = new WebContainerLoginModule();
            module.initialize(subject, handler, 
                              new HashMap<String, Object>(), 
                              new HashMap<String, Object>());
            module.login();
            module.commit();
            Set principals = subject.getPrincipals();
            assertEquals( 1, principals.size() );
            assertTrue(  principals.contains( principal ) );
            assertFalse( principals.contains( Role.ANONYMOUS ) );
            assertFalse( principals.contains( Role.ASSERTED ) );
            assertFalse( principals.contains( Role.AUTHENTICATED ) );
            assertFalse( principals.contains( Role.ALL ) );
        }
        catch( LoginException e )
        {
            System.err.println( e.getMessage() );
            assertTrue( false );
        }
    }

    public final void testLoginWithRoles() throws Exception
    {
        // Create user with 2 container roles; TestAuthorizer knows about these
        Principal principal = new WikiPrincipal( "Andrew Jaquith" );
        MockHttpServletRequest request = m_engine.newHttpRequest();
        request.setUserPrincipal( principal );
        Set<String> roles = new HashSet<String>();
        roles.add( "IT" );
        roles.add( "Engineering" );
        request.setRoles( roles );

        // Test using Principal (WebContainerLoginModule succeeds)
        CallbackHandler handler = new WebContainerCallbackHandler( m_engine, request, authorizer );
        LoginModule module = new WebContainerLoginModule();
        module.initialize(subject, handler, 
                          new HashMap<String, Object>(), 
                          new HashMap<String, Object>());
        module.login();
        module.commit();
        Set principals = subject.getPrincipals();
        assertEquals( 3, principals.size() );
        assertTrue( principals.contains( principal ) );
        assertFalse( principals.contains( Role.ANONYMOUS ) );
        assertFalse( principals.contains( Role.ASSERTED ) );
        assertFalse( principals.contains( Role.AUTHENTICATED ) );
        assertFalse( principals.contains( Role.ALL ) );
        assertTrue(  principals.contains( new Role( "IT" ) ) );
        assertTrue(  principals.contains( new Role( "Engineering" ) ) );
    }

    public final void testLogout()
    {
        Principal principal = new WikiPrincipal( "Andrew Jaquith" );
        MockHttpServletRequest request = m_engine.newHttpRequest();
        request.setUserPrincipal( principal );
        try
        {
            CallbackHandler handler = new WebContainerCallbackHandler( m_engine, request, authorizer );
            LoginModule module = new WebContainerLoginModule();
            module.initialize(subject, handler, 
                              new HashMap<String, Object>(), 
                              new HashMap<String, Object>());
            module.login();
            module.commit();
            Set principals = subject.getPrincipals();
            assertEquals( 1, principals.size() );
            assertTrue( principals.contains( principal ) );
            assertFalse( principals.contains( Role.AUTHENTICATED ) );
            assertFalse( principals.contains( Role.ALL ) );
            module.logout();
            assertEquals( 0, principals.size() );
        }
        catch( LoginException e )
        {
            System.err.println( e.getMessage() );
            assertTrue( false );
        }
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        Properties props = new Properties();
        props.load( TestEngine.findTestProperties() );
        props.put(XMLUserDatabase.PROP_USERDATABASE, "tests/etc/userdatabase.xml");
        m_engine = new TestEngine(props);
        authorizer = new TestAuthorizer();
        authorizer.initialize( m_engine, props );
        db = new XMLUserDatabase();
        subject = new Subject();
        try
        {
            db.initialize( m_engine, props );
        }
        catch( NoRequiredPropertyException e )
        {
            System.err.println( e.getMessage() );
            assertTrue( false );
        }
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        m_engine.shutdown();
    }

}