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
import org.jdom2.JDOMException;

import java.io.PrintWriter;
import java.util.Stack;


/**
 * Translates to wiki syntax from a plain text. Specifically, this decorator handles the following conversions, when needed:
 * <ul>
 *     <li>Bold elements</li>
 *     <li>Italic elements</li>
 *     <li>Monospace elements</li>
 *     <li>Css classes</li>
 *     <li>Css styles</li>
 * </ul>
 */
public class PlainTextDecorator {

    final protected PrintWriter out;
    final protected Stack< String > preStack;
    final protected XHtmlElementToWikiTranslator chain;
    final protected PlainTextCssDecorator ptcd;

    public PlainTextDecorator( final PlainTextCssDecorator ptcd, final PrintWriter out, final Stack< String > preStack, final XHtmlElementToWikiTranslator chain ) {
        this.out = out;
        this.preStack = preStack;
        this.chain = chain;
        this.ptcd = ptcd;
    }

    /**
     * Translates the given XHTML element into wiki markup.
     *
     * @param dto XHTML element being translated.
     */
    public void decorate( final XHtmlElementToWikiTranslator.ElementDecoratorData dto ) throws JDOMException {
        ptcd.decorate( dto );
    }

}
