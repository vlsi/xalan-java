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
package org.apache.xml.utils;

/**
 * <meta name="usage" content="internal"/>
 * A very simple table that stores a list of int. Very similar API to our
 * IntVector class (same API); different internal storage.
 * 
 * This version uses an array-of-arrays solution. Read/write access is thus
 * a bit slower than the simple IntVector, and basic storage is a trifle
 * higher due to the top-level array -- but appending is O(1) fast rather
 * than O(N**2) slow, which will swamp those costs in situations where
 * long vectors are being built up.
 * 
 * Known issues:
 * 
 * Some methods are private because they haven't yet been tested properly.
 * 
 * If an element has not been set (because we skipped it), its value will
 * initially be 0. Shortening the vector does not clear old storage; if you
 * then skip values and setElementAt a higher index again, you may see old data
 * reappear in the truncated-and-restored section. Doing anything else would
 * have performance costs.
 */
public class SuballocatedIntVector
{
  /** Size of blocks to allocate          */
  protected int m_blocksize;
  
  /** Number of blocks to (over)allocate by */
  protected  int m_numblocks=32;
  
  /** Array of arrays of ints          */
  protected int m_map[][];

  /** Number of ints in array          */
  protected int m_firstFree = 0;

  /** "Shortcut" handle to m_map[0] */
  protected int m_map0[];

  /**
   * Default constructor.  Note that the default
   * block size is very small, for small lists.
   */
  public SuballocatedIntVector()
  {
    this(2048);
  }

  /**
   * Construct a IntVector, using the given block size.
   *
   * @param blocksize Size of block to allocate
   */
  public SuballocatedIntVector(int blocksize)
  {
    m_blocksize = blocksize;
    m_map0=new int[blocksize];
    m_map = new int[m_numblocks][];
    m_map[0]=m_map0;
  }
  
  /**
   * Construct a IntVector, using the given block size.
   *
   * @param blocksize Size of block to allocate
   */
  public SuballocatedIntVector(int blocksize, int increaseSize)
  {
    // increaseSize not currently used.
    this(blocksize);
  }


  /**
   * Get the length of the list.
   *
   * @return length of the list
   */
  public int size()
  {
    return m_firstFree;
  }
  
  /**
   * Set the length of the list.
   *
   * @return length of the list
   */
  private  void setSize(int sz)
  {
    if(m_firstFree<sz)
      m_firstFree = sz;
  }

  /**
   * Append a int onto the vector.
   *
   * @param value Int to add to the list 
   */
  public  void addElement(int value)
  {
    if(m_firstFree<m_blocksize)
      m_map0[m_firstFree++]=value;
    else
    {
      int index=m_firstFree/m_blocksize;
      int offset=m_firstFree%m_blocksize;
      ++m_firstFree;

      if(index>=m_map.length)
      {
        int newsize=index+m_numblocks;
        int[][] newMap=new int[newsize][];
        System.arraycopy(m_map, 0, newMap, 0, m_map.length);
        m_map=newMap;
      }
      int[] block=m_map[index];
      if(null==block)
        block=m_map[index]=new int[m_blocksize];
      block[offset]=value;
    }
  }
  
  /**
   * Append several int values onto the vector.
   *
   * @param value Int to add to the list 
   */
  private  void addElements(int value, int numberOfElements)
  {
    if(m_firstFree+numberOfElements<m_blocksize)
      for (int i = 0; i < numberOfElements; i++) 
      {
        m_map0[m_firstFree++]=value;
      }
    else
    {
      int index=m_firstFree/m_blocksize;
      int offset=m_firstFree%m_blocksize;
      m_firstFree+=numberOfElements;
      while( numberOfElements>0)
      {
        if(index>=m_map.length)
        {
          int newsize=index+m_numblocks;
          int[][] newMap=new int[newsize][];
          System.arraycopy(m_map, 0, newMap, 0, m_map.length);
          m_map=newMap;
        }
        int[] block=m_map[index];
        if(null==block)
          block=m_map[index]=new int[m_blocksize];
        int copied=(m_blocksize-offset < numberOfElements)
          ? m_blocksize-offset : numberOfElements;
        numberOfElements-=copied;
        while(copied-- > 0)
          block[offset++]=value;

        ++index;offset=0;
      }
    }
  }
  
  /**
   * Append several slots onto the vector, but do not set the values.
   * Note: "Not Set" means the value is unspecified.
   *
   * @param value Int to add to the list 
   */
  private  void addElements(int numberOfElements)
  {
    int newlen=m_firstFree+numberOfElements;
    if(newlen>m_blocksize)
    {
      int index=m_firstFree%m_blocksize;
      int newindex=(m_firstFree+numberOfElements)%m_blocksize;
      for(int i=index+1;i<=newindex;++i)
        m_map[i]=new int[m_blocksize];
    }
    m_firstFree=newlen;
  }
  
  /**
   * Inserts the specified node in this vector at the specified index.
   * Each component in this vector with an index greater or equal to
   * the specified index is shifted upward to have an index one greater
   * than the value it had previously.
   *
   * Insertion may be an EXPENSIVE operation!
   *
   * @param value Int to insert
   * @param at Index of where to insert 
   */
  private  void insertElementAt(int value, int at)
  {
    if(at==m_firstFree)
      addElement(value);
	else if (at>m_firstFree)
	{
      int index=at/m_blocksize;
      if(index>=m_map.length)
      {
        int newsize=index+m_numblocks;
        int[][] newMap=new int[newsize][];
        System.arraycopy(m_map, 0, newMap, 0, m_map.length);
        m_map=newMap;
      }
      int[] block=m_map[index];
      if(null==block)
        block=m_map[index]=new int[m_blocksize];
      int offset=at%m_blocksize;
	  block[offset]=value;
	  m_firstFree=offset+1;
	}
    else
    {
      int index=at/m_blocksize;
      int maxindex=m_firstFree+1/m_blocksize;
      ++m_firstFree;
      int offset=at%m_blocksize;
      int push;
      
      // ***** Easier to work down from top?
      while(index<=maxindex)
      {
        int copylen=m_blocksize-offset-1;
        int[] block=m_map[index];
        if(null==block)
        {
          push=0;
          block=m_map[index]=new int[m_blocksize];
        }
        else
        {
          push=block[m_blocksize-1];
          System.arraycopy(block, offset , block, offset+1, copylen);
        }
        block[offset]=value;
        value=push;
        offset=0;
        ++index;
      }
    }
  }

  /**
   * Wipe it out. Old version set everytying to Integer.MIN_VALUE.
   * Do we really need that here?
   */
  public void removeAllElements()
  {
    // for(int index=m_map.length;index>=0;--index)
    //{
    //  int[] block=m_map[index];
    //  if(null!=block)
    //  for(int offset=m_blocksize;offset>=0;--offset)
    //    block[offset] = java.lang.Integer.MIN_VALUE;
    //}
    m_firstFree = 0;
  }

  /**
   * Removes the first occurrence of the argument from this vector.
   * If the object is found in this vector, each component in the vector
   * with an index greater or equal to the object's index is shifted
   * downward to have an index one smaller than the value it had
   * previously.
   *
   * @param s Int to remove from array
   *
   * @return True if the int was removed, false if it was not found
   */
  private  boolean removeElement(int s)
  {
    int at=indexOf(s,0);
    if(at<0)
      return false;
    removeElementAt(at);
    return true;
  }

  /**
   * Deletes the component at the specified index. Each component in
   * this vector with an index greater or equal to the specified
   * index is shifted downward to have an index one smaller than
   * the value it had previously.
   *
   * @param i index of where to remove and int
   */
  private  void removeElementAt(int at)
  {
	// No point in removing elements that "don't exist"...  
    if(at<m_firstFree)
    {
      int index=at/m_blocksize;
      int maxindex=m_firstFree/m_blocksize;
      int offset=at%m_blocksize;
      
      while(index<=maxindex)
      {
        int copylen=m_blocksize-offset-1;
        int[] block=m_map[index];
        if(null==block)
          block=m_map[index]=new int[m_blocksize];
        else
          System.arraycopy(block, offset+1, block, offset, copylen);
        if(index<maxindex)
        {
          int[] next=m_map[index+1];
          if(next!=null)
            block[m_blocksize-1]=(next!=null) ? next[0] : 0;
        }
        else
          block[m_blocksize-1]=0;
        offset=0;
        ++index;
      }
    }
    --m_firstFree;
  }

  /**
   * Sets the component at the specified index of this vector to be the
   * specified object. The previous component at that position is discarded.
   *
   * The index must be a value greater than or equal to 0 and less
   * than the current size of the vector.
   *
   * @param node object to set
   * @param index Index of where to set the object
   */
  public void setElementAt(int value, int at)
  {
    if(at<m_blocksize)
    {
      m_map0[at]=value;
      return;
    }

    int index=at/m_blocksize;
    int offset=at%m_blocksize;
	
    if(index>=m_map.length)
    {
      int newsize=index+m_numblocks;
      int[][] newMap=new int[newsize][];
      System.arraycopy(m_map, 0, newMap, 0, m_map.length);
      m_map=newMap;
    }

	int[] block=m_map[index];
    if(null==block)
      block=m_map[index]=new int[m_blocksize];
    block[offset]=value;

    if(at>=m_firstFree)
      m_firstFree=at+1;
  }

  /**
   * Get the nth element.
   *
   * @param i index of object to get
   *
   * @return object at given index
   */
  public int elementAt(int i)
  {
    if(i<m_blocksize)
      return m_map0[i];
	if(i>=m_firstFree)
		return Integer.MIN_VALUE; // %REVIEW% Does anyone _care_?
    
    int index=i/m_blocksize;
    int offset=i%m_blocksize;

    int[] block=m_map[index];
    if(null==block)
      return Integer.MIN_VALUE; // %REVIEW% Does anyone _care_?
    return block[offset];
  }

  /**
   * Tell if the table contains the given node.
   *
   * @param s object to look for
   *
   * @return true if the object is in the list
   */
  private  boolean contains(int s)
  {
    return (indexOf(s,0) >= 0);
  }

  /**
   * Searches for the first occurence of the given argument,
   * beginning the search at index, and testing for equality
   * using the equals method.
   *
   * @param elem object to look for
   * @param index Index of where to begin search
   * @return the index of the first occurrence of the object
   * argument in this vector at position index or later in the
   * vector; returns -1 if the object is not found.
   */
  public int indexOf(int elem, int index)
  {
	if(index>=m_firstFree)
		return -1;
	  
    int bindex=index/m_blocksize;
    int boffset=index%m_blocksize;
    int maxindex=m_firstFree/m_blocksize;
    int[] block;
    
    for(;bindex<maxindex;++bindex)
    {
      block=m_map[bindex];
      if(block!=null)
        for(int offset=boffset;offset<m_blocksize;++offset)
          if(block[offset]==elem)
            return offset+bindex*m_blocksize;
      boffset=0; // after first
    }
    // Last block may need to stop before end
    int maxoffset=m_firstFree%m_blocksize;
    block=m_map[maxindex];
    for(int offset=boffset;offset<maxoffset;++offset)
      if(block[offset]==elem)
        return offset+maxindex*m_blocksize;

    return -1;    
  }

  /**
   * Searches for the first occurence of the given argument,
   * beginning the search at index, and testing for equality
   * using the equals method.
   *
   * @param elem object to look for
   * @return the index of the first occurrence of the object
   * argument in this vector at position index or later in the
   * vector; returns -1 if the object is not found.
   */
  public int indexOf(int elem)
  {
    return indexOf(elem,0);
  }

  /**
   * Searches for the first occurence of the given argument,
   * beginning the search at index, and testing for equality
   * using the equals method.
   *
   * @param elem Object to look for
   * @return the index of the first occurrence of the object
   * argument in this vector at position index or later in the
   * vector; returns -1 if the object is not found.
   */
  private  int lastIndexOf(int elem)
  {
    int boffset=m_firstFree%m_blocksize;
    for(int index=m_firstFree/m_blocksize;
        index>=0;
        --index)
    {
      int[] block=m_map[index];
      if(block!=null)
        for(int offset=boffset; offset>=0; --offset)
          if(block[offset]==elem)
            return offset+index*m_blocksize;
      boffset=0; // after first
    }
    return -1;
  }

}
