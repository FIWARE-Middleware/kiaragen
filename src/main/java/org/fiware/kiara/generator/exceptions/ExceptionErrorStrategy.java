/* KIARA - Middleware for efficient and QoS/Security-aware invocation of services and exchange of messages
 *
 * Copyright (C) 2014 Proyectos y Sistemas de Mantenimiento S.L. (eProsima)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fiware.kiara.generator.exceptions;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

/**
*
* @author Rafael Lara {@literal <rafaellara@eprosima.com>}
*/
public class ExceptionErrorStrategy extends DefaultErrorStrategy {

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        throw e;
    }

    @Override
    public void reportInputMismatch(Parser recognizer, InputMismatchException e) throws RecognitionException {
        String msg = "";
        msg += "In file " + recognizer.getSourceName() + " at line " + recognizer.getContext().start.getLine() + ": ";
        msg += "Mismatched input " + getTokenErrorDisplay(e.getOffendingToken());
        msg += " expecting one of "+e.getExpectedTokens().toString(recognizer.getTokenNames()) + "\n";
        msg += "Line Number " + recognizer.getContext().start.getLine() + ", Column " + recognizer.getContext().start.getCharPositionInLine() + ";";
        RecognitionException ex = new RecognitionException(msg, recognizer, recognizer.getInputStream(), recognizer.getContext());
        ex.initCause(e);
        throw ex;
    }

    @Override
    public void reportMissingToken(Parser recognizer) {
        beginErrorCondition(recognizer);
        Token t = recognizer.getCurrentToken();
        IntervalSet expecting = getExpectedTokens(recognizer);
        String msg = "";
        msg += "In file " + recognizer.getSourceName() + " at line " + recognizer.getContext().start.getLine() + ": ";
        msg += "Missing "+expecting.toString(recognizer.getTokenNames()) + " at " + getTokenErrorDisplay(t) + ";";
        //msg += "Line Number " + recognizer.getContext().start.getLine() + ", Column " + recognizer.getContext().start.getCharPositionInLine() + ";";
        throw new RecognitionException(msg, recognizer, recognizer.getInputStream(), recognizer.getContext());
    }
    
}
