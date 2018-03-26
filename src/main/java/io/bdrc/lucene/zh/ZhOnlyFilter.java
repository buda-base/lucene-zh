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


import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

/**
 * Filters all tokens whose type is not "IDEOGRAPHIC" or "CJ",
 * the types produced by {@link org#apache#lucene#analysis#standard#StandardTokenizer} for Chinese tokens.
 * 
 * @author Hélios Hildt
 */
public class ZhOnlyFilter extends TokenFilter {

  public ZhOnlyFilter(TokenStream in) {
    super(in);
  }
  
  protected TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
  
  @Override
  public final boolean incrementToken() throws IOException {
      while (input.incrementToken()) {
          if (typeAtt.type().equals("<IDEOGRAPHIC>") || typeAtt.type().equals("<CJ>")) {
              return true;
          } else {
              continue;
          }
      }
      return false;
  }
}
