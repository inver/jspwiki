/* 
    JSPWiki - a JSP-based WikiWiki clone.

    Copyright (C) 2003 Janne Jalkanen (Janne.Jalkanen@iki.fi)

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.ecyrd.jspwiki.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.modules.ModuleManager;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;

/**
 *  This class takes care of managing JSPWiki templates.
 *
 *  @since 2.1.62
 *  @author Janne Jalkanen
 */
public class TemplateManager 
    extends ModuleManager
{
    /**
     * Requests a JavaScript function to be called during window.onload.
     */
    public static final String RESOURCE_JSFUNCTION = "jsfunction";

    /**
     * Requests a stylesheet to be inserted.
     */
    public static final String RESOURCE_STYLESHEET = "stylesheet";

    /**
     * Requests a script to be loaded.
     */
    public static final String RESOURCE_SCRIPT     = "script";

    /** The default directory for the properties. */
    public static final String DIRECTORY    = "templates";

    public static final String DEFAULT_TEMPLATE = "default";

    /** Name of the file that contains the properties.*/

    public static final String PROPERTYFILE = "template.properties";

    public static final String RESOURCE_INCLUDES = "jspwiki.resourceincludes";
    
    private WikiEngine         m_engine;
    private Cache              m_propertyCache;

    protected static Logger log = Logger.getLogger( TemplateManager.class );

    public TemplateManager( WikiEngine engine, Properties properties )
    {
        m_engine = engine;

        //
        //  Uses the unlimited cache.
        //
        // m_propertyCache = new Cache( true, false );
    }

    /**
     *  Check the existence of a template.
     */
    // FIXME: Does not work yet
    public boolean templateExists( String templateName )
    {
        ServletContext context = m_engine.getServletContext();

        InputStream in = context.getResourceAsStream( getPath(templateName)+"ViewTemplate.jsp");

        if( in != null )
        {
            try
            {
                in.close();
            }
            catch( IOException e ) {}

            return true;
        }

        return false;
    }

    /**
     *  An utility method for finding a JSP page.  It searches only under
     *  either current context or by the absolute name.
     */
    public String findJSP( PageContext pageContext, String name )
    {
        ServletContext sContext = pageContext.getServletContext();
        
        InputStream is = sContext.getResourceAsStream( name );

        if( is == null )
        {
            String defname = makeFullJSPName( DEFAULT_TEMPLATE, 
                                              removeTemplatePart(name) );
            is = sContext.getResourceAsStream( defname );

            if( is != null )
                name = defname;
            else
                name = null;
        }

        if( is != null ) try { is.close(); } catch( IOException e ) {}

        return name;
    }

    /**
     *  Removes the template part of a name.
     */
    private final String removeTemplatePart( String name )
    {
        int idx = name.indexOf('/');
        if( idx != -1 )
        {
            idx = name.indexOf('/', idx); // Find second "/"

            if( idx != -1 )
            {
                return name.substring( idx+1 );
            }
        }
        
        return name;
    }

    private final String makeFullJSPName( String template, String name )
    {
        return "/"+DIRECTORY+"/"+template+"/"+name;
    }

    /**
     *  Attempts to locate a JSP page under the given template.  If that template
     *  does not exist, or the page does not exist under that template, will
     *  attempt to locate a similarly named file under the default template.
     */
    public String findJSP( PageContext pageContext, String template, String name )
    {
        ServletContext sContext = pageContext.getServletContext();

        if( name.charAt(0) == '/' )
        {
            // This is already a full path
            return findJSP( pageContext, name );
        }

        String fullname = makeFullJSPName( template, name );
        InputStream is = sContext.getResourceAsStream( fullname );

        if( is == null )
        {
            String defname = makeFullJSPName( DEFAULT_TEMPLATE, name );
            is = sContext.getResourceAsStream( defname );

            if( is != null )
                fullname = defname;
            else
                fullname = null;
        }

        if( is != null ) try { is.close(); } catch( IOException e ) {}

        return fullname;
    }

    /**
     *  Returns a property, as defined in the template.  The evaluation
     *  is lazy, i.e. the properties are not loaded until the template is
     *  actually used for the first time.
     */
    public String getTemplateProperty( WikiContext context, String key )
    {
        String template = context.getTemplate();

        try
        {
            Properties props = (Properties)m_propertyCache.getFromCache( template, -1 );

            if( props == null )
            {
                try
                {
                    props = getTemplateProperties( template );
                    
                    m_propertyCache.putInCache( template, props );
                }
                catch( IOException e )
                {
                    log.warn("IO Exception while reading template properties",e);

                    return null;
                }
            }

            return props.getProperty( key );
        }
        catch( NeedsRefreshException ex )
        {
            // FIXME
            return null;
        }
    }

    private static final String getPath( String template )
    {
        return "/"+DIRECTORY+"/"+template+"/";
    }

    /**
     *   Lists the skins available under this template.  Returns an 
     *   empty Set, if there are no extra skins available.  Note that
     *   this method does not check whether there is anything actually
     *   in the directories, it just lists them.  This may change
     *   in the future.
     *   
     *   @param template
     *   @return Set of Strings with the skin names.
     *   @since 2.3.26
     */
    public Set listSkins( PageContext pageContext, String template )
    {
        String place = makeFullJSPName( template, "skins" );
     
        ServletContext sContext = pageContext.getServletContext();

        Set skinSet = sContext.getResourcePaths( place );
        TreeSet resultSet = new TreeSet();
               
        log.debug( "Listings skins from "+place );
        
        if( skinSet != null )
        {
            String[] skins = {};
            
            skins = (String[]) skinSet.toArray(skins);        
        
            for( int i = 0; i < skins.length; i++ )
            {
                String s[] = StringUtils.split(skins[i],"/");
            
                if( s.length > 1 )
                {
                    String skinName = s[s.length-1];
                    resultSet.add( skinName );
                    log.debug("..."+skinName);
                }
            }
        }
        
        return resultSet;
    }

    /**
     *  Always returns a valid property map.
     */
    private Properties getTemplateProperties( String templateName )
        throws IOException
    {
        Properties p = new Properties();

        ServletContext context = m_engine.getServletContext();

        InputStream propertyStream = context.getResourceAsStream(getPath(templateName)+PROPERTYFILE);

        if( propertyStream != null )
        {
            p.load( propertyStream );

            propertyStream.close();
        }
        else
        {
            log.debug("Template '"+templateName+"' does not have a propertyfile '"+PROPERTYFILE+"'.");
        }

        return p;
    }
    
    /**
     *  Returns the include resources marker for a given type.  This is in a
     *  HTML comment format.
     *   
     *  @param type
     *  @return
     */
    public static String getMarker( String type )
    {
        if( type.equals(RESOURCE_JSFUNCTION) )
        {
            return "/* INCLUDERESOURCES ("+type+") */";
        }
        return "<!-- INCLUDERESOURCES ("+type+") -->";
    }

    /**
     *  Adds a resource request to the current request context.
     */
    
    public static void addResourceRequest( WikiContext ctx, String type, String path )
    {
        HashMap resourcemap = (HashMap) ctx.getVariable( RESOURCE_INCLUDES );
       
        if( resourcemap == null )
        {
            resourcemap = new HashMap();
        }
        
        Vector resources = (Vector) resourcemap.get( type );
        
        if( resources == null )
        {
            resources = new Vector();
        }
        
        String resourceString = null;
        
        if( type == RESOURCE_SCRIPT )
        {
            resourceString = "<script type='text/javascript' src='"+path+"'></script>";
        }
        else if( type == RESOURCE_STYLESHEET )
        {
            resourceString = "<link rel='stylesheet' type='text/css' src='"+path+"' />";
        }
        else if( type == RESOURCE_JSFUNCTION )
        {
            resourceString = path;
        }
        
        if( resourceString != null )
        {
            resources.add( resourceString );
        }
        
        log.debug("Request to add a resource: "+resourceString);
        
        resourcemap.put( type, resources );
        ctx.setVariable( RESOURCE_INCLUDES, resourcemap );
    }
    
    /**
     *  Returns resource requests for a particular type.  If there are no resources,
     *  returns an empty array.
     */
    
    public static String[] getResourceRequests( WikiContext ctx, String type )
    {
        HashMap hm = (HashMap) ctx.getVariable( RESOURCE_INCLUDES );
        
        if( hm == null ) return new String[0];
        
        Vector resources = (Vector) hm.get( type );
        
        if( resources == null ) return new String[0];
        
        String[] res = new String[resources.size()];
        
        return (String[]) resources.toArray( res );
    }
    
    /**
     *  returns all those types that have been requested so far.
     *  
     * @param ctx
     * @return
     */
    public static String[] getResourceTypes( WikiContext ctx )
    {
        String[] res = new String[0];

        if( ctx != null )
        {
            HashMap hm = (HashMap) ctx.getVariable( RESOURCE_INCLUDES );
        
            if( hm != null )
            {
                Set keys = hm.keySet();
            
                res = (String[]) keys.toArray( res );
            }
        }
        
        return res;
    }
}
