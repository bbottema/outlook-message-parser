/*
 * msgparser - http://auxilii.com/msgparser
 * Copyright (C) 2007  Roman Kurmanowytsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.auxilii.msgparser.rtf;

/**
 * This interface defines the structure of the conversion class to be used for extracting HTML code out of an RTF body
 * in case no pure HTML body was found. By default the msgparser uses the built-in {@link SimpleRTF2HTMLConverter} class
 * but it can be replaced by a custom implementation.
 * 
 * @author thomas.misar
 * 
 */
public interface RTF2HTMLConverter {

	public String rtf2html(String rtf) throws Exception;

}