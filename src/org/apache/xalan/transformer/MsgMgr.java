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
package org.apache.xalan.transformer;

import org.apache.xalan.res.XSLMessages;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;

/**
 * This class will manage error messages, warning messages, and other types of
 * message events.
 */
public class MsgMgr
{

  /**
   * Create a message manager object.
   *
   * NEEDSDOC @param transformer
   */
  public MsgMgr(TransformerImpl transformer)
  {
    m_transformer = transformer;
  }

  /** NEEDSDOC Field m_transformer          */
  private TransformerImpl m_transformer;

  /** NEEDSDOC Field m_XSLMessages          */
  private static XSLMessages m_XSLMessages = new XSLMessages();

  /**
   * Warn the user of an problem.
   * This is public for access by extensions.
   *
   * NEEDSDOC @param msg
   * NEEDSDOC @param terminate
   * @exception XSLProcessorException thrown if the active ProblemListener and XPathContext decide
   * the error condition is severe enough to halt processing.
   *
   * @throws SAXException
   */
  public void message(String msg, boolean terminate) throws SAXException
  {

    ErrorHandler errHandler = m_transformer.getErrorHandler();

    if (null != errHandler)
    {
      if (terminate)
        errHandler.fatalError(new SAXParseException(msg, new LocatorImpl()));
      else
        errHandler.warning(new SAXParseException(msg, new LocatorImpl()));
    }
    else
    {
      if (terminate)
        throw new SAXException(msg);
      else
        System.out.println(msg);
    }
  }

  /**
   * <meta name="usage" content="internal"/>
   * Warn the user of an problem.
   *
   * NEEDSDOC @param msg
   * @exception XSLProcessorException thrown if the active ProblemListener and XPathContext decide
   * the error condition is severe enough to halt processing.
   *
   * @throws SAXException
   */
  public void warn(int msg) throws SAXException
  {
    warn(null, null, msg, null);
  }

  /**
   * <meta name="usage" content="internal"/>
   * Warn the user of an problem.
   *
   * NEEDSDOC @param msg
   * NEEDSDOC @param args
   * @exception XSLProcessorException thrown if the active ProblemListener and XPathContext decide
   * the error condition is severe enough to halt processing.
   *
   * @throws SAXException
   */
  public void warn(int msg, Object[] args) throws SAXException
  {
    warn(null, null, msg, args);
  }

  /**
   * <meta name="usage" content="internal"/>
   * Warn the user of an problem.
   *
   * NEEDSDOC @param styleNode
   * NEEDSDOC @param sourceNode
   * NEEDSDOC @param msg
   * @exception XSLProcessorException thrown if the active ProblemListener and XPathContext decide
   * the error condition is severe enough to halt processing.
   *
   * @throws SAXException
   */
  public void warn(Node styleNode, Node sourceNode, int msg)
          throws SAXException
  {
    warn(styleNode, sourceNode, msg, null);
  }

  /**
   * <meta name="usage" content="internal"/>
   * Warn the user of an problem.
   *
   * NEEDSDOC @param styleNode
   * NEEDSDOC @param sourceNode
   * NEEDSDOC @param msg
   * NEEDSDOC @param args
   * @exception XSLProcessorException thrown if the active ProblemListener and XPathContext decide
   * the error condition is severe enough to halt processing.
   *
   * @throws SAXException
   */
  public void warn(Node styleNode, Node sourceNode, int msg, Object args[])
          throws SAXException
  {

    String formattedMsg = m_XSLMessages.createWarning(msg, args);
    ErrorHandler errHandler = m_transformer.getErrorHandler();

    if (null != errHandler)
      errHandler.warning(new SAXParseException(formattedMsg, new LocatorImpl()));
    else
      System.out.println(formattedMsg);
  }

  /**
   * <meta name="usage" content="internal"/>
   * Tell the user of an error, and probably throw an
   * exception.
   *
   * NEEDSDOC @param msg
   * @exception XSLProcessorException thrown if the active ProblemListener and XPathContext decide
   * the error condition is severe enough to halt processing.
   *
   * @throws SAXException
   */
  public void error(String msg) throws SAXException
  {

    // Locator locator = m_stylesheetLocatorStack.isEmpty()
    //                  ? null :
    //                    ((Locator)m_stylesheetLocatorStack.peek());
    // Locator locator = null;
    ErrorHandler errHandler = m_transformer.getErrorHandler();

    if (null != errHandler)
      errHandler.fatalError(new SAXParseException(msg, new LocatorImpl()));
    else
      throw new SAXException(msg);
  }

  /**
   * <meta name="usage" content="internal"/>
   * Tell the user of an error, and probably throw an
   * exception.
   *
   * NEEDSDOC @param msg
   * @exception XSLProcessorException thrown if the active ProblemListener and XPathContext decide
   * the error condition is severe enough to halt processing.
   *
   * @throws SAXException
   */
  public void error(int msg) throws SAXException
  {
    error(null, null, msg, null);
  }

  /**
   * <meta name="usage" content="internal"/>
   * Tell the user of an error, and probably throw an
   * exception.
   *
   * NEEDSDOC @param msg
   * NEEDSDOC @param args
   * @exception XSLProcessorException thrown if the active ProblemListener and XPathContext decide
   * the error condition is severe enough to halt processing.
   *
   * @throws SAXException
   */
  public void error(int msg, Object[] args) throws SAXException
  {
    error(null, null, msg, args);
  }

  /**
   * <meta name="usage" content="internal"/>
   * Tell the user of an error, and probably throw an
   * exception.
   *
   * NEEDSDOC @param msg
   * NEEDSDOC @param e
   * @exception XSLProcessorException thrown if the active ProblemListener and XPathContext decide
   * the error condition is severe enough to halt processing.
   *
   * @throws SAXException
   */
  public void error(int msg, Exception e) throws SAXException
  {
    error(msg, null, e);
  }

  /**
   * <meta name="usage" content="internal"/>
   * Tell the user of an error, and probably throw an
   * exception.
   *
   * NEEDSDOC @param msg
   * NEEDSDOC @param args
   * NEEDSDOC @param e
   * @exception XSLProcessorException thrown if the active ProblemListener and XPathContext decide
   * the error condition is severe enough to halt processing.
   *
   * @throws SAXException
   */
  public void error(int msg, Object args[], Exception e) throws SAXException
  {

    //msg  = (null == msg) ? XSLTErrorResources.ER_PROCESSOR_ERROR : msg;
    String formattedMsg = m_XSLMessages.createMessage(msg, args);

    // Locator locator = m_stylesheetLocatorStack.isEmpty()
    //                   ? null :
    //                    ((Locator)m_stylesheetLocatorStack.peek());
    // Locator locator = null;
    ErrorHandler errHandler = m_transformer.getErrorHandler();

    if (null != errHandler)
      errHandler.fatalError(new SAXParseException(formattedMsg, new LocatorImpl()));
    else
      throw new SAXException(formattedMsg);
  }

  /**
   *  <meta name="usage" content="internal"/>
   * Tell the user of an error, and probably throw an
   * exception.
   *
   * NEEDSDOC @param styleNode
   * NEEDSDOC @param sourceNode
   * NEEDSDOC @param msg
   * @exception XSLProcessorException thrown if the active ProblemListener and XPathContext decide
   * the error condition is severe enough to halt processing.
   *
   * @throws SAXException
   */
  public void error(Node styleNode, Node sourceNode, int msg)
          throws SAXException
  {
    error(styleNode, sourceNode, msg, null);
  }

  /**
   * <meta name="usage" content="internal"/>
   * Tell the user of an error, and probably throw an
   * exception.
   *
   * NEEDSDOC @param styleNode
   * NEEDSDOC @param sourceNode
   * NEEDSDOC @param msg
   * NEEDSDOC @param args
   * @exception XSLProcessorException thrown if the active ProblemListener and XPathContext decide
   * the error condition is severe enough to halt processing.
   *
   * @throws SAXException
   */
  public void error(Node styleNode, Node sourceNode, int msg, Object args[])
          throws SAXException
  {

    String formattedMsg = m_XSLMessages.createMessage(msg, args);

    // Locator locator = m_stylesheetLocatorStack.isEmpty()
    //                   ? null :
    //                    ((Locator)m_stylesheetLocatorStack.peek());
    // Locator locator = null;
    ErrorHandler errHandler = m_transformer.getErrorHandler();

    if (null != errHandler)
      errHandler.warning(new SAXParseException(formattedMsg, new LocatorImpl()));
    else
      throw new SAXException(formattedMsg);
  }
}
