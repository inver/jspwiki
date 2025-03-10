/*
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
package org.apache.wiki.htmltowiki.syntax;

import org.apache.wiki.htmltowiki.XHtmlElementToWikiTranslator;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.PrintWriter;
import java.util.Stack;


/**
 * Translates to wiki syntax from a {@code TH} element.
 */
public abstract class ThDecorator {

    final protected PrintWriter out;
    final protected Stack< String > preStack;
    final protected XHtmlElementToWikiTranslator chain;

    protected ThDecorator( final PrintWriter out, final Stack< String > preStack, final XHtmlElementToWikiTranslator chain ) {
        this.out = out;
        this.preStack = preStack;
        this.chain = chain;
    }

    /**
     * Translates the given XHTML element into wiki markup.
     *
     * @param element XHTML element being translated.
     */
    public void decorate( final Element element ) throws JDOMException {
        out.print( markupTh() );
        chain.translate( element );
        if( preStack.isEmpty() ) {
            out.print( " " );
        }
    }

    /**
     * Wiki markup for a {@code TH} element.
     *
     * @return Wiki markup for a {@code TH} element.
     */
    protected abstract String markupTh();

}
