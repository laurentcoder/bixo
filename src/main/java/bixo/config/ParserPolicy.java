/*
 * Copyright (c) 2010 TransPac Software, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package bixo.config;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Set;

import bixo.parser.BaseLinkExtractor;

/**
 * Definition of policy for parsing.
 * 
 */
@SuppressWarnings("serial")
public class ParserPolicy implements Serializable {
    
    public static final int NO_MAX_PARSE_DURATION = Integer.MAX_VALUE;
    
    public static final int DEFAULT_MAX_PARSE_DURATION = 30 * 1000;
    
    private int _maxParseDuration;        // Max # of milliseconds to wait for parse to complete a document.
    
    private Set<String> _linkTags;
    
    private Set<String> _linkAttributeTypes;
    
    public ParserPolicy() {
        this(DEFAULT_MAX_PARSE_DURATION);
    }

    public ParserPolicy(int maxParseDuration) {
        this(   maxParseDuration,
                BaseLinkExtractor.DEFAULT_LINK_TAGS,
                BaseLinkExtractor.DEFAULT_LINK_ATTRIBUTE_TYPES);
    }

    public ParserPolicy(int maxParseDuration,
                        Set<String> linkTags,
                        Set<String> linkAttributeTypes) {
        if ((maxParseDuration <= 0) && (maxParseDuration != NO_MAX_PARSE_DURATION)) {
            throw new InvalidParameterException("maxParseDuration must be > 0: " + maxParseDuration);
        }
        
        // Catch common error of specifying maxParseDuration in seconds versus milliseconds
        if (maxParseDuration < 100)  {
            throw new InvalidParameterException("maxParseDuration must be milliseconds, not seconds: " + maxParseDuration);
        }
        
        _maxParseDuration = maxParseDuration;
        _linkAttributeTypes = linkAttributeTypes;
        _linkTags = linkTags;
    }

    public int getMaxParseDuration() {
        return _maxParseDuration;
    }
       
    public void setMaxParseDuration(int maxParseDuration) {
        _maxParseDuration = maxParseDuration;
    }

    public Set<String> getLinkTags() {
        return _linkTags;
    }

    public void setLinkTags(Set<String> linkTags) {
        _linkTags = linkTags;
    }

    public Set<String> getLinkAttributeTypes() {
        return _linkAttributeTypes;
    }

    public void setLinkAttributeTypes(Set<String> linkAttributeTypes) {
        _linkAttributeTypes = linkAttributeTypes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_linkAttributeTypes == null) ? 0 : _linkAttributeTypes.hashCode());
        result = prime * result + ((_linkTags == null) ? 0 : _linkTags.hashCode());
        result = prime * result + _maxParseDuration;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParserPolicy other = (ParserPolicy) obj;
        if (_linkAttributeTypes == null) {
            if (other._linkAttributeTypes != null)
                return false;
        } else if (!_linkAttributeTypes.equals(other._linkAttributeTypes))
            return false;
        if (_linkTags == null) {
            if (other._linkTags != null)
                return false;
        } else if (!_linkTags.equals(other._linkTags))
            return false;
        if (_maxParseDuration != other._maxParseDuration)
            return false;
        return true;
    }

    @Override
	public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Max parse duration: " + getMaxParseDuration());
        result.append('\r');
        result.append("Link tags: " + getLinkTags());
        result.append('\r');
        result.append("Link attribute types: " + getLinkAttributeTypes());
        
        return result.toString();
    }
}
