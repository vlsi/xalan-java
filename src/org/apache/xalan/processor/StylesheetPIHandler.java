/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xalan.processor;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;

import java.util.Vector;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.apache.xalan.utils.SystemIDResolver;

/**
 * Handle the xml-stylesheet processing instruction.
 * @see <a href="http://www.w3.org/TR/xml-stylesheet/">Associating Style Sheets with XML documents, Version 1.0</a>
 */
public class StylesheetPIHandler extends DefaultHandler
{

  /** NEEDSDOC Field STARTELEM_FOUND_MSG          */
  static final String STARTELEM_FOUND_MSG = "##startElement found";

  /** NEEDSDOC Field m_baseID          */
  String m_baseID;

  /** NEEDSDOC Field m_media          */
  String m_media;

  /** NEEDSDOC Field m_title          */
  String m_title;

  /** NEEDSDOC Field m_charset          */
  String m_charset;

  /** NEEDSDOC Field m_stylesheets          */
  Vector m_stylesheets = new Vector();

  /**
   * Construct a StylesheetPIHandler instance.
   *
   * NEEDSDOC @param source
   * NEEDSDOC @param media
   * NEEDSDOC @param title
   * NEEDSDOC @param charset
   */
  public StylesheetPIHandler(String baseID, String media, String title,
                             String charset)
  {

    m_baseID = baseID;
    m_media = media;
    m_title = title;
    m_charset = charset;
  }

  /**
   * Return all stylesheets found that match the constraints.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public Source getAssociatedStylesheet()
  {

    int sz = m_stylesheets.size();

    if (sz > 0)
    {
      SAXSource ssource 
        = new SAXSource((InputSource) m_stylesheets.elementAt(sz-1));
      return ssource;
    }
    else
      return null;
  }

  /**
   * Handle the xml-stylesheet processing instruction.
   *
   * @param target The processing instruction target.
   * @param data The processing instruction data, or null if
   *             none is supplied.
   * @exception org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#processingInstruction
   *
   * @throws SAXException
   * @see <a href="http://www.w3.org/TR/xml-stylesheet/">Associating Style Sheets with XML documents, Version 1.0</a>
   */
  public void processingInstruction(String target, String data)
          throws SAXException
  {

    if (target.equals("xml-stylesheet"))
    {
      String href = null;  // CDATA #REQUIRED
      String type = null;  // CDATA #REQUIRED
      String title = null;  // CDATA #IMPLIED
      String media = null;  // CDATA #IMPLIED
      String charset = null;  // CDATA #IMPLIED
      boolean alternate = false;  // (yes|no) "no"
      StringTokenizer tokenizer = new StringTokenizer(data, " \t=");

      while (tokenizer.hasMoreTokens())
      {
        String name = tokenizer.nextToken();

        if (name.equals("type"))
        {
          String typeVal = tokenizer.nextToken();

          type = typeVal.substring(1, typeVal.length() - 1);
        }
        else if (name.equals("href"))
        {
          href = tokenizer.nextToken();
          href = href.substring(1, href.length() - 1);
          href = SystemIDResolver.getAbsoluteURI(href, m_baseID);
        }
        else if (name.equals("title"))
        {
          title = tokenizer.nextToken();
          title = title.substring(1, title.length() - 1);
        }
        else if (name.equals("media"))
        {
          media = tokenizer.nextToken();
          media = media.substring(1, media.length() - 1);
        }
        else if (name.equals("charset"))
        {
          charset = tokenizer.nextToken();
          charset = charset.substring(1, charset.length() - 1);
        }
        else if (name.equals("alternate"))
        {
          String alternateStr = tokenizer.nextToken();

          alternate = alternateStr.substring(1, alternateStr.length()
                                             - 1).equals("yes");
        }
      }

      if ((null != type) && type.equals("text/xsl") && (null != href))
      {
        if (null != m_media)
        {
          if (null != media)
          {
            if (!media.equals(m_media))
              return;
          }
          else
            return;
        }

        if (null != m_charset)
        {
          if (null != charset)
          {
            if (!charset.equals(m_charset))
              return;
          }
          else
            return;
        }

        if (null != m_title)
        {
          if (null != title)
          {
            if (!title.equals(m_title))
              return;
          }
          else
            return;
        }

        m_stylesheets.addElement(new InputSource(href));
      }
    }
  }

  /**
   * The spec notes that "The xml-stylesheet processing instruction is allowed only in the prolog of an XML document.",
   * so, at least for right now, I'm going to go ahead an throw a SAXException
   * in order to stop the parse.
   *
   * NEEDSDOC @param namespaceURI
   * NEEDSDOC @param localName
   * NEEDSDOC @param qName
   * NEEDSDOC @param atts
   *
   * @throws SAXException
   */
  public void startElement(
          String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException
  {
    throw new StopParseException();
  }
}
