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
package org.apache.xpath.objects;

//import org.w3c.dom.Node;
//import org.w3c.dom.Text;
//import org.w3c.dom.DocumentFragment;
//import org.w3c.dom.traversal.NodeIterator;

import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;

import org.apache.xpath.DOMHelper;
import org.apache.xpath.XPathContext;
import org.apache.xpath.NodeSet;
import org.apache.xpath.axes.ContextNodeList;
import org.apache.xml.utils.StringVector;

/**
 * <meta name="usage" content="general"/>
 * This class represents an XPath nodeset object, and is capable of
 * converting the nodeset to other types, such as a string.
 */
public class XNodeSet extends XObject
{
  private DTMManager m_dtmMgr;
  
  public DTMManager getDTMMgr()
  {
    return m_dtmMgr;
  }

  /**
   * Construct a XNodeSet object.
   *
   * @param val Value of the XNodeSet object
   */
  public XNodeSet(DTMIterator val)
  {
    super(val);
    m_dtmMgr = val.getDTMManager();
  }

  /**
   * Construct an empty XNodeSet object.  This is used to create a mutable 
   * nodeset to which random nodes may be added.
   */
  public XNodeSet(DTMManager dtmMgr)
  {
    super(new NodeSet());
    m_dtmMgr = dtmMgr;
  }

  /**
   * Construct a XNodeSet object for one node.
   *
   * @param n Node to add to the new XNodeSet object
   */
  public XNodeSet(int n, DTMManager dtmMgr)
  {

    super(new NodeSet());
    m_dtmMgr = dtmMgr;

    if (DTM.NULL != n)
    {
      ((NodeSet) m_obj).addNode(n);
    }
  }

  /**
   * Tell that this is a CLASS_NODESET.
   *
   * @return type CLASS_NODESET
   */
  public int getType()
  {
    return CLASS_NODESET;
  }

  /**
   * Given a request type, return the equivalent string.
   * For diagnostic purposes.
   *
   * @return type string "#NODESET"
   */
  public String getTypeString()
  {
    return "#NODESET";
  }

  /**
   * Get numeric value of the string conversion from a single node.
   *
   * @param n Node to convert
   *
   * @return numeric value of the string conversion from a single node.
   */
  public double getNumberFromNode(int n)
  {
    return XString.castToNum(getStringFromNode(n));
  }

  /**
   * Cast result object to a number.
   *
   * @return numeric value of the string conversion from the 
   * next node in the NodeSet, or NAN if no node was found
   */
  public double num()
  {

    DTMIterator nl = nodeset();
    int node = nl.nextNode();

    return (node != DTM.NULL) ? getNumberFromNode(node) : Double.NaN;
  }

  /**
   * Cast result object to a boolean.
   *
   * @return True if there is a next node in the nodeset
   */
  public boolean bool()
  {
    return (nodeset().nextNode() != DTM.NULL);
  }
  
  /**
   * Get the string conversion from a single node.
   *
   * @param n Node to convert
   *
   * @return the string conversion from a single node.
   */
  public String getStringFromNode(int n)
  {
    // %OPT%
    // I guess we'll have to get a static instance of the DTM manager...
    if(DTM.NULL != n)
    {
      return m_dtmMgr.getDTM(n).getStringValue(n);
    }
    else
    {
      return "";
    }
  }

  /**
   * Cast result object to a string.
   *
   * @return the string conversion from the next node in the nodeset
   * or "" if there is no next node
   */
  public String str()
  {

    DTMIterator nl = nodeset();
    int node = nl.nextNode();

    return (node != DTM.NULL) ? getStringFromNode(node) : "";
  }

  // %REVIEW%
//  /**
//   * Cast result object to a result tree fragment.
//   *
//   * @param support The XPath context to use for the conversion 
//   *
//   * @return the nodeset as a result tree fragment.
//   */
//  public DTMIterator rtree(XPathContext support)
//  {
//    DTM frag = support.createDocumentFragment();
//
//    DTMIterator nl = nodeset();
//    int node;
//
//    while (DTM.NULL != (node = nl.nextNode()))
//    {
//      frag.appendChild(node, true, true);
//    }
//
//    return support.createDTMIterator(frag.getDocument());
//  }
  
  /**
   * Cast result object to a nodelist.
   *
   * @return The nodeset as a nodelist
   */
  public DTMIterator nodeset()
  {

    // System.out.println("In XNodeSet.nodeset()");
    DTMIterator ns = (DTMIterator) m_obj;

    // System.out.println("Is a ContextNodeList: "+ns);
    if (ns.isFresh())  // bit of a hack...
    {
      return ns;
    }
    else
    {
      try
      {
        return ns.cloneWithReset();
      }
      catch (CloneNotSupportedException cnse)
      {
        throw new RuntimeException(cnse.getMessage());
      }
    }
  }

  /**
   * Cast result object to a mutableNodeset.
   *
   * @return The nodeset as a mutableNodeset
   */
  public NodeSet mutableNodeset()
  {

    NodeSet mnl;

    if(m_obj instanceof NodeSet)
    {
      mnl = (NodeSet) m_obj;
    }
    else
    {
      mnl = new NodeSet(nodeset());
      m_obj = mnl;
    }

    return mnl;
  }

  /** Less than comparator         */
  static LessThanComparator S_LT = new LessThanComparator();

  /** Less than or equal comparator          */
  static LessThanOrEqualComparator S_LTE = new LessThanOrEqualComparator();

  /** Greater than comparator         */
  static GreaterThanComparator S_GT = new GreaterThanComparator();

  /** Greater than or equal comparator          */
  static GreaterThanOrEqualComparator S_GTE =
    new GreaterThanOrEqualComparator();

  /** Equal comparator         */
  static EqualComparator S_EQ = new EqualComparator();

  /** Not equal comparator         */
  static NotEqualComparator S_NEQ = new NotEqualComparator();

  /**
   * Tell if one object is less than the other.
   *
   * @param obj2 Object to compare this nodeset to
   * @param comparator Comparator to use
   *
   * @return See the comments below for each object type comparison 
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean compare(XObject obj2, Comparator comparator)
          throws javax.xml.transform.TransformerException
  {

    boolean result = false;
    int type = obj2.getType();

    if (XObject.CLASS_NODESET == type)
    {

      // From http://www.w3.org/TR/xpath: 
      // If both objects to be compared are node-sets, then the comparison 
      // will be true if and only if there is a node in the first node-set 
      // and a node in the second node-set such that the result of performing 
      // the comparison on the string-values of the two nodes is true.
      // Note this little gem from the draft:
      // NOTE: If $x is bound to a node-set, then $x="foo" 
      // does not mean the same as not($x!="foo"): the former 
      // is true if and only if some node in $x has the string-value 
      // foo; the latter is true if and only if all nodes in $x have 
      // the string-value foo.
      DTMIterator list1 = nodeset();
      DTMIterator list2 = ((XNodeSet) obj2).nodeset();
      int node1;
      StringVector node2Strings = null;

      while (DTM.NULL != (node1 = list1.nextNode()))
      {
        String s1 = getStringFromNode(node1);

        if (null == node2Strings)
        {
          int node2;

          while (DTM.NULL != (node2 = list2.nextNode()))
          {
            String s2 = getStringFromNode(node2);

            if (comparator.compareStrings(s1, s2))
            {
              result = true;

              break;
            }

            if (null == node2Strings)
              node2Strings = new StringVector();

            node2Strings.addElement(s2);
          }
        }
        else
        {
          int n = node2Strings.size();

          for (int i = 0; i < n; i++)
          {
            if (comparator.compareStrings(s1, node2Strings.elementAt(i)))
            {
              result = true;

              break;
            }
          }
        }
      }
    }
    else if (XObject.CLASS_BOOLEAN == type)
    {

      // From http://www.w3.org/TR/xpath: 
      // If one object to be compared is a node-set and the other is a boolean, 
      // then the comparison will be true if and only if the result of 
      // performing the comparison on the boolean and on the result of 
      // converting the node-set to a boolean using the boolean function 
      // is true.
      double num1 = bool() ? 1.0 : 0.0;
      double num2 = obj2.num();

      result = comparator.compareNumbers(num1, num2);
    }
    else if (XObject.CLASS_NUMBER == type)
    {

      // From http://www.w3.org/TR/xpath: 
      // If one object to be compared is a node-set and the other is a number, 
      // then the comparison will be true if and only if there is a 
      // node in the node-set such that the result of performing the 
      // comparison on the number to be compared and on the result of 
      // converting the string-value of that node to a number using 
      // the number function is true. 
      DTMIterator list1 = nodeset();
      double num2 = obj2.num();
      int node;

      while (DTM.NULL != (node = list1.nextNode()))
      {
        double num1 = getNumberFromNode(node);

        if (comparator.compareNumbers(num1, num2))
        {
          result = true;

          break;
        }
      }
    }
    else if (XObject.CLASS_RTREEFRAG == type)
    {

      // hmmm... 
      // Try first to treat it as a number, so that numeric 
      // comparisons can be done with it.  I suspect this is bogus...
      double num2 = obj2.num();

      if (!Double.isNaN(num2))
      {
        DTMIterator list1 = nodeset();
        int node;

        while (DTM.NULL != (node = list1.nextNode()))
        {
          double num1 = getNumberFromNode(node);

          if (comparator.compareNumbers(num1, num2))
          {
            result = true;

            break;
          }
        }
      }
      else
      {
        String s2 = obj2.str();
        DTMIterator list1 = nodeset();
        int node;

        while (DTM.NULL != (node = list1.nextNode()))
        {
          String s1 = getStringFromNode(node);

          if (comparator.compareStrings(s1, s2))
          {
            result = true;

            break;
          }
        }
      }
    }
    else if (XObject.CLASS_STRING == type)
    {

      // From http://www.w3.org/TR/xpath: 
      // If one object to be compared is a node-set and the other is a 
      // string, then the comparison will be true if and only if there 
      // is a node in the node-set such that the result of performing 
      // the comparison on the string-value of the node and the other 
      // string is true. 
      String s2 = obj2.str();
      DTMIterator list1 = nodeset();
      int node;

      while (DTM.NULL != (node = list1.nextNode()))
      {
        String s1 = getStringFromNode(node);

        if (comparator.compareStrings(s1, s2))
        {
          result = true;

          break;
        }
      }
    }
    else
    {
      result = comparator.compareNumbers(this.num(), obj2.num());
    }

    return result;
  }

  /**
   * Tell if one object is less than the other.
   *
   * @param obj2 object to compare this nodeset to
   *
   * @return see this.compare(...) 
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean lessThan(XObject obj2) throws javax.xml.transform.TransformerException
  {
    return compare(obj2, S_LT);
  }

  /**
   * Tell if one object is less than or equal to the other.
   *
   * @param obj2 object to compare this nodeset to
   *
   * @return see this.compare(...) 
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean lessThanOrEqual(XObject obj2) throws javax.xml.transform.TransformerException
  {
    return compare(obj2, S_LTE);
  }

  /**
   * Tell if one object is less than the other.
   *
   * @param obj2 object to compare this nodeset to
   *
   * @return see this.compare(...) 
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean greaterThan(XObject obj2) throws javax.xml.transform.TransformerException
  {
    return compare(obj2, S_GT);
  }

  /**
   * Tell if one object is less than the other.
   *
   * @param obj2 object to compare this nodeset to
   *
   * @return see this.compare(...) 
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean greaterThanOrEqual(XObject obj2)
          throws javax.xml.transform.TransformerException
  {
    return compare(obj2, S_GTE);
  }

  /**
   * Tell if two objects are functionally equal.
   *
   * @param obj2 object to compare this nodeset to
   *
   * @return see this.compare(...) 
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean equals(XObject obj2) throws javax.xml.transform.TransformerException
  {
    return compare(obj2, S_EQ);
  }

  /**
   * Tell if two objects are functionally not equal.
   *
   * @param obj2 object to compare this nodeset to
   *
   * @return see this.compare(...) 
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean notEquals(XObject obj2) throws javax.xml.transform.TransformerException
  {
    return compare(obj2, S_NEQ);
  }
}

/**
 * compares nodes for various boolean operations.
 */
abstract class Comparator
{

  /**
   * Compare two strings
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare 
   *
   * @return Whether the strings are equal or not
   */
  abstract boolean compareStrings(String s1, String s2);

  /**
   * Compare two numbers
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return Whether the numbers are equal or not
   */
  abstract boolean compareNumbers(double n1, double n2);
}

/**
 * Compare strings or numbers for less than.
 */
class LessThanComparator extends Comparator
{

  /**
   * Compare two strings for less than.
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare 
   *
   * @return True if s1 is less than s2
   */
  boolean compareStrings(String s1, String s2)
  {
    return s1.compareTo(s2) < 0;
  }

  /**
   * Compare two numbers for less than.
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return true if n1 is less than n2
   */
  boolean compareNumbers(double n1, double n2)
  {
    return n1 < n2;
  }
}

/**
 * Compare strings or numbers for less than or equal.
 */
class LessThanOrEqualComparator extends Comparator
{

  /**
   * Compare two strings for less than or equal.
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare
   *
   * @return true if s1 is less than or equal to s2
   */
  boolean compareStrings(String s1, String s2)
  {
    return s1.compareTo(s2) <= 0;
  }

  /**
   * Compare two numbers for less than or equal.
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return true if n1 is less than or equal to n2
   */
  boolean compareNumbers(double n1, double n2)
  {
    return n1 <= n2;
  }
}

/**
 * Compare strings or numbers for greater than.
 */
class GreaterThanComparator extends Comparator
{

  /**
   * Compare two strings for greater than.
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare
   *
   * @return true if s1 is greater than s2
   */
  boolean compareStrings(String s1, String s2)
  {
    return s1.compareTo(s2) > 0;
  }

  /**
   * Compare two numbers for greater than.
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return true if n1 is greater than n2
   */
  boolean compareNumbers(double n1, double n2)
  {
    return n1 > n2;
  }
}

/**
 * Compare strings or numbers for greater than or equal.
 */
class GreaterThanOrEqualComparator extends Comparator
{

  /**
   * Compare two strings for greater than or equal.
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare
   *
   * @return true if s1 is greater than or equal to s2
   */
  boolean compareStrings(String s1, String s2)
  {
    return s1.compareTo(s2) >= 0;
  }

  /**
   * Compare two numbers for greater than or equal.
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return true if n1 is greater than or equal to n2
   */
  boolean compareNumbers(double n1, double n2)
  {
    return n1 >= n2;
  }
}

/**
 * Compare strings or numbers for equality.
 */
class EqualComparator extends Comparator
{

  /**
   * Compare two strings for equality.
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare
   *
   * @return true if s1 is equal to s2
   */
  boolean compareStrings(String s1, String s2)
  {
    return s1.equals(s2);
  }

  /**
   * Compare two numbers for equality.
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return true if n1 is equal to n2
   */
  boolean compareNumbers(double n1, double n2)
  {
    return n1 == n2;
  }
}

/**
 * Compare strings or numbers for non-equality.
 */
class NotEqualComparator extends Comparator
{

  /**
   * Compare two strings for non-equality.
   *
   *
   * @param s1 First string to compare
   * @param s2 Second String to compare
   *
   * @return true if s1 is not equal to s2
   */
  boolean compareStrings(String s1, String s2)
  {
    return !s1.equals(s2);
  }

  /**
   * Compare two numbers for non-equality.
   *
   *
   * @param n1 First number to compare
   * @param n2 Second number to compare
   *
   * @return true if n1 is not equal to n2
   */
  boolean compareNumbers(double n1, double n2)
  {
    return n1 != n2;
  }
}
