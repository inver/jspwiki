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
package org.apache.wiki.htmltowiki.syntax.jspwiki;

import org.apache.wiki.htmltowiki.XHtmlElementToWikiTranslator;
import org.apache.wiki.htmltowiki.syntax.PlainTextBoldDecorator;
import org.apache.wiki.htmltowiki.syntax.PlainTextItalicDecorator;

import java.io.PrintWriter;
import java.util.Stack;


/**
 * JSPWiki syntax implementation of {@link PlainTextBoldDecorator}.
 */
class JSPWikiPlainTextBoldDecorator extends PlainTextBoldDecorator {

    JSPWikiPlainTextBoldDecorator( final PlainTextItalicDecorator ptid, final PrintWriter out, final Stack< String > preStack, final XHtmlElementToWikiTranslator chain ) {
        super( ptid, out, preStack, chain );
    }

    /** {@inheritDoc} */
    @Override
    protected String markupBoldOpen() {
        return "__";
    }

    /** {@inheritDoc} */
    @Override
    protected String markupBoldClose() {
        return "__";
    }

}
