/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * Author:  NikitaFeodonit, nfeodonit@yandex.com
 * *****************************************************************************
 * Copyright (c) 2015-2015. NextGIS, info@nextgis.com
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.forestinspector.util;

import android.text.InputFilter;
import android.text.Spanned;


// http://stackoverflow.com/a/14212734/4727406
public abstract class InputFilterMinMax<E extends Comparable<E>>
        implements InputFilter
{
    protected E mMin, mMax;


    public InputFilterMinMax(
            E min,
            E max)
    {
        mMin = min;
        mMax = max;
    }


    public InputFilterMinMax(
            String min,
            String max)
    {
        mMin = parseVal(min);
        mMax = parseVal(max);
    }


    // http://stackoverflow.com/a/19072151/4727406
    @Override
    public CharSequence filter(
            CharSequence source,
            int start,
            int end,
            Spanned dest,
            int dstart,
            int dend)
    {
        try {
            // Remove the string out of destination that is to be replaced
            String newVal = dest.toString().substring(0, dstart) +
                            dest.toString().substring(dend, dest.toString().length());
            // Add the new string in
            newVal = newVal.substring(0, dstart) + source.toString() +
                     newVal.substring(dstart, newVal.length());
            E input = parseVal(newVal);
            if (isInRange(mMin, mMax, input)) {
                return null;
            }
        } catch (NumberFormatException nfe) {
        }
        return "";
    }


    protected boolean isInRange(
            E a,
            E b,
            E c)
    {
//        return b > a
//               ? c >= a && c <= b
//               : c >= b && c <= a;
        return b.compareTo(a) > 0
               ? c.compareTo(a) >= 0 && c.compareTo(b) <= 0
               : c.compareTo(b) >= 0 && c.compareTo(a) <= 0;
    }


    protected abstract E parseVal(String stringVal);
}
