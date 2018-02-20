/*******************************************************************************
 * Copyright (c) 2018 Buddhist Digital Resource Center (BDRC)
 * 
 * If this file is a derivation of another work the license header will appear 
 * below; otherwise, this work is licensed under the Apache License, Version 2.0 
 * (the "License"); you may not use this file except in compliance with the 
 * License.
 * 
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package io.bdrc.lucene.zh;


import org.apache.lucene.analysis.util.CharTokenizer;

/**
 *
 */
public class CnTokenizer extends CharTokenizer {
  
  /**
   * Creates a new {@link CnTokenizer} instance
   */
  public CnTokenizer() {
  }
  
  @Override
  protected boolean isTokenChar(int c) {
      return (char) c != ' ' && (char) c != '。';
  }
  
  /**
   * Called on each token character to normalize it before it is added to the
   * token. The default implementation does nothing. Subclasses may use this to,
   * e.g., lowercase tokens.
   */
  @Override
  protected int normalize(int c) {
    return c;
  }
}
