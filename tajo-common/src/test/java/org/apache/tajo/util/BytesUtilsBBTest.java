/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tajo.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class BytesUtilsBBTest {

    // =========================================================================
    // SEZIONE 1: Test Frame per il metodo split()
    // =========================================================================

    // TF1: Nominale (C2)
    @Test
    public void testSplitNominal() {
        byte[] str = "a,b,c".getBytes();
        byte[] sep = ",".getBytes();
        int[][] indices = new int[4][];

        int[][] result = BytesUtils.split(str, 0, 5, sep, indices);

        assertSame(indices, result);
        assertArrayEquals(new int[]{0, 1}, result[0]);
        assertArrayEquals(new int[]{2, 3}, result[1]);
        assertArrayEquals(new int[]{4, 5}, result[2]);
        assertNull(result[3]);
    }

    // TF2: str = null
    @Test(expected = NullPointerException.class)
    public void testSplitNullString() {
        byte[] sep = ",".getBytes();
        int[][] indices = new int[4][];
        BytesUtils.split(null, 0, 5, sep, indices);
    }

    // TF3: str vuota, offset=0, length=0 (C1)
    @Test
    public void testSplitEmptyStringAndLength() {
        byte[] str = new byte[0];
        byte[] sep = ",".getBytes();
        int[][] indices = new int[4][];

        int[][] result = BytesUtils.split(str, 0, 0, sep, indices);
        assertArrayEquals(new int[]{0, 0}, result[0]);
        assertNull(result[1]);
        assertNull(result[2]);
        assertNull(result[3]);
    }

    // TF4: str="a", offset=0, length=1 (C2 sull'ultimo byte)
    @Test
    public void testSplitSingleByteString() {
        byte[] str = "a".getBytes();
        byte[] sep = ",".getBytes();
        int[][] indices = new int[4][];

        int[][] result = BytesUtils.split(str, 0, 1, sep, indices);
        assertArrayEquals(new int[]{0, 1}, result[0]);
        assertNull(result[1]);
        assertNull(result[2]);
        assertNull(result[3]);
    }

    // TF5: separatore null
    @Test(expected = NullPointerException.class)
    public void testSplitNullSeparator() {
        byte[] str = "a,b,c".getBytes();
        int[][] indices = new int[4][];
        BytesUtils.split(str, 0, 5, null, indices);
    }

    // TF6: separatore vuoto (len 0)
    @Ignore
    @Test
    public void testSplitEmptySeparator() {
        byte[] str = "a,b,c".getBytes();
        byte[] sep = new byte[0];
        int[][] indices = new int[4][];

        int[][] result = BytesUtils.split(str, 0, 5, sep, indices);
        assertArrayEquals(new int[]{0, 1}, result[0]);
        assertArrayEquals(new int[]{1, 2}, result[1]);
        assertArrayEquals(new int[]{2, 3}, result[2]);
        assertArrayEquals(new int[]{3, 4}, result[3]);
    }

    // TF7: separatore multi-byte
    @Test
    public void testSplitMultiByteSeparator() {
        byte[] str = "a::b::c".getBytes();
        byte[] sep = "::".getBytes();
        int[][] indices = new int[4][];

        int[][] result = BytesUtils.split(str, 0, 7, sep, indices);
        assertArrayEquals(new int[]{0, 1}, result[0]);
        assertArrayEquals(new int[]{3, 4}, result[1]);
        assertArrayEquals(new int[]{6, 7}, result[2]);
        assertNull(result[3]);
    }

    // TF8: separatore assente
    @Test
    public void testSplitSeparatorAbsent() {
        byte[] str = "abc".getBytes();
        byte[] sep = ",".getBytes();
        int[][] indices = new int[4][];

        int[][] result = BytesUtils.split(str, 0, 3, sep, indices);
        assertArrayEquals(new int[]{0, 3}, result[0]);
        assertNull(result[1]);
        assertNull(result[2]);
        assertNull(result[3]);
    }

    // TF9: indices = null
    @Test(expected = NullPointerException.class)
    public void testSplitNullIndices() {
        byte[] str = "a,b,c".getBytes();
        byte[] sep = ",".getBytes();
        BytesUtils.split(str, 0, 5, sep, null);
    }

    // TF10: indices capienza 0
    @Test
    public void testSplitZeroCapacityIndices() {
        byte[] str = "a,b,c".getBytes();
        byte[] sep = ",".getBytes();
        int[][] indices = new int[0][];

        int[][] result = BytesUtils.split(str, 0, 5, sep, indices);
        assertEquals(0, result.length);
    }

    // TF11: saturazione indices (N - 1)
    @Test
    public void testSplitIndicesEarlySaturation() {
        byte[] str = "a,b,c".getBytes();
        byte[] sep = ",".getBytes();
        int[][] indices = new int[2][];

        int[][] result = BytesUtils.split(str, 0, 5, sep, indices);
        assertEquals(2, result.length);
        assertArrayEquals(new int[]{0, 1}, result[0]);
        assertArrayEquals(new int[]{2, 3}, result[1]);
    }

    // TF12: capienza esatta indices (N)
    @Test
    public void testSplitIndicesExactCapacity() {
        byte[] str = "a,b,c".getBytes();
        byte[] sep = ",".getBytes();
        int[][] indices = new int[3][];

        int[][] result = BytesUtils.split(str, 0, 5, sep, indices);
        assertEquals(3, result.length);
        assertArrayEquals(new int[]{4, 5}, result[2]);
    }

    // TF13: offset < 0 (C0 invalido)
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testSplitNegativeOffset() {
        byte[] str = "a,b,c".getBytes();
        byte[] sep = ",".getBytes();
        int[][] indices = new int[4][];
        BytesUtils.split(str, -1, 5, sep, indices);
    }

    // TF14: length < 0 (C0 invalido) - Pure Black-Box
    @Ignore
    @Test(expected = Exception.class)
    public void testSplitNegativeLength() {
        byte[] str = "a,b,c".getBytes();
        byte[] sep = ",".getBytes();
        int[][] indices = new int[4][];

        BytesUtils.split(str, 0, -1, sep, indices);
    }

    // TF15: offset = L, length = 0 (C1 finestra vuota a fine array)
    @Test
    public void testSplitEmptyWindowAtEnd() {
        byte[] str = "a,b,c".getBytes();
        byte[] sep = ",".getBytes();
        int[][] indices = new int[4][];

        int[][] result = BytesUtils.split(str, 5, 0, sep, indices);
        assertArrayEquals(new int[]{5, 5}, result[0]);
        assertNull(result[1]);
        assertNull(result[2]);
        assertNull(result[3]);
    }

    // TF16: finestra di 1 solo byte (C2 finestra minima)
    @Test
    public void testSplitMinimalWindow() {
        byte[] str = "a,b,c".getBytes();
        byte[] sep = ",".getBytes();
        int[][] indices = new int[4][];

        int[][] result = BytesUtils.split(str, 0, 1, sep, indices);
        assertArrayEquals(new int[]{0, 1}, result[0]);
        assertNull(result[1]);
        assertNull(result[2]);
        assertNull(result[3]);
    }

    // TF17: sforamento di 1 byte (C3)
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testSplitLengthOutOfBounds() {
        byte[] str = "a,b,c".getBytes();
        byte[] sep = ",".getBytes();
        int[][] indices = new int[4][];
        BytesUtils.split(str, 0, 6, sep, indices);
    }

    // =========================================================================
    // SEZIONE 2: Test Frame per il metodo writeUtf8() (Aggiornati a 24 Frame)
    // =========================================================================

    // WF1: Min 1-byte (Caso Base nominale)
    @Test
    public void testWriteUtf8_WF1_BaseNominal() {
        char[] chars = {'\u0000'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, false);
        assertEquals(1, written);
    }

    // WF2: Min 1-byte con variazione del flag ignoreSurrogate a true
    @Test
    public void testWriteUtf8_WF2_NominalIgnoreTrue() {
        char[] chars = {'\u0000'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, true);
        assertEquals(1, written);
    }

    // WF3: chars = null
    @Test(expected = NullPointerException.class)
    public void testWriteUtf8_WF3_NullChars() {
        ByteBuf buffer = Unpooled.buffer(10);
        BytesUtils.writeUtf8(buffer, null, false);
    }

    // WF4: chars vuoto (lunghezza 0)
    @Test
    public void testWriteUtf8_WF4_EmptyChars() {
        char[] chars = new char[0];
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, false);
        assertEquals(0, written);
    }

    // WF5: buffer = null
    @Test(expected = NullPointerException.class)
    public void testWriteUtf8_WF5_NullBuffer() {
        char[] chars = {'\u0000'};
        BytesUtils.writeUtf8(null, chars, false);
    }

    // WF6: buffer con capacità insufficiente -> Sforamento
    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteUtf8_WF6_InsufficientBuffer() {
        char[] chars = {'\u0000'};
        ByteBuf buffer = Unpooled.buffer(0, 0);
        BytesUtils.writeUtf8(buffer, chars, false);
    }

    // WF7: buffer con capacità esatta (Nb)
    @Test
    public void testWriteUtf8_WF7_ExactBuffer() {
        char[] chars = {'\u0000'};
        ByteBuf buffer = Unpooled.buffer(1, 1);
        int written = BytesUtils.writeUtf8(buffer, chars, false);
        assertEquals(1, written);
    }

    // WF8: Limite max 1-byte (U+007F)
    @Test
    public void testWriteUtf8_WF8_MaxOneByte() {
        char[] chars = {'\u007F'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, false);
        assertEquals(1, written);
    }

    // WF9: Limite min 2-byte (U+0080)
    @Test
    public void testWriteUtf8_WF9_MinTwoBytes() {
        char[] chars = {'\u0080'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, false);
        assertEquals(2, written);
    }

    // WF10: Limite max 2-byte (U+07FF)
    @Test
    public void testWriteUtf8_WF10_MaxTwoBytes() {
        char[] chars = {'\u07FF'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, false);
        assertEquals(2, written);
    }

    // WF11: Limite min 3-byte (U+0800)
    @Test
    public void testWriteUtf8_WF11_MinThreeBytes() {
        char[] chars = {'\u0800'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, false);
        assertEquals(3, written);
    }

    // WF12: Max 3-byte pre-buco (U+D7FF)
    @Test
    public void testWriteUtf8_WF12_MaxThreeBytesPreHole() {
        char[] chars = {'\uD7FF'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, false);
        assertEquals(3, written);
    }

    // WF13: Min Spaiato Alto (U+D800) -> Eccezione
    @Test(expected = Exception.class)
    public void testWriteUtf8_WF13_MinUnpairedHigh() {
        char[] chars = {'\uD800'};
        ByteBuf buffer = Unpooled.buffer(10);
        BytesUtils.writeUtf8(buffer, chars, false);
    }

    // WF14: Max Spaiato Alto (U+DBFF) -> Eccezione
    @Test(expected = Exception.class)
    public void testWriteUtf8_WF14_MaxUnpairedHigh() {
        char[] chars = {'\uDBFF'};
        ByteBuf buffer = Unpooled.buffer(10);
        BytesUtils.writeUtf8(buffer, chars, false);
    }

    // WF15: Min Spaiato Basso (U+DC00) -> Eccezione
    @Test(expected = Exception.class)
    public void testWriteUtf8_WF15_MinUnpairedLow() {
        char[] chars = {'\uDC00'};
        ByteBuf buffer = Unpooled.buffer(10);
        BytesUtils.writeUtf8(buffer, chars, false);
    }

    // WF16: Max Spaiato Basso (U+DFFF) -> Eccezione
    @Test(expected = IllegalArgumentException.class)
    public void testWriteUtf8_WF16_MaxUnpairedLow() {
        char[] chars = {'\uDFFF'};
        ByteBuf buffer = Unpooled.buffer(10);
        BytesUtils.writeUtf8(buffer, chars, false);
    }

    // WF17: Min 3-byte post-buco (U+E000)
    @Test
    public void testWriteUtf8_WF17_MinThreeBytesPostHole() {
        char[] chars = {'\uE000'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, false);
        assertEquals(3, written);
    }

    // WF18: Max assoluto 3-byte (U+FFFF)
    @Test
    public void testWriteUtf8_WF18_MaxAbsoluteThreeBytes() {
        char[] chars = {'\uFFFF'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, false);
        assertEquals(3, written);
    }

    // WF19: Coppia Valida (Unificato in 4 byte)
    @Test
    public void testWriteUtf8_WF19_ValidSurrogatePair() {
        char[] chars = {'\uD800', '\uDC00'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, false);
        assertEquals(4, written);
    }

    // WF20: Coppia Valida con ignore=true (Tollerato, 6 byte)
    @Test
    public void testWriteUtf8_WF20_ValidSurrogatePairIgnored() {
        char[] chars = {'\uD800', '\uDC00'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, true);
        assertEquals(6, written); // Nessuna eccezione
    }

    // WF21: Coppia Invertita -> Eccezione
    @Test(expected = Exception.class)
    public void testWriteUtf8_WF21_InvertedSurrogatePair() {
        char[] chars = {'\uDC00', '\uD800'};
        ByteBuf buffer = Unpooled.buffer(10);
        BytesUtils.writeUtf8(buffer, chars, false);
    }

    // WF22: Coppia Invertita con ignore=true (Tollerato, 6 byte)
    @Test
    public void testWriteUtf8_WF22_InvertedSurrogatePairIgnored() {
        char[] chars = {'\uDC00', '\uD800'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, true);
        assertEquals(6, written); // Nessuna eccezione
    }

    // WF23: Spaiato Alto con ignore=true (Tollerato, 3 byte)
    @Test
    public void testWriteUtf8_WF23_UnpairedHighIgnored() {
        char[] chars = {'\uD800'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, true);
        assertEquals(3, written); // Nessuna eccezione
    }

    // WF24: Spaiato Basso con ignore=true (Tollerato, 3 byte)
    @Test
    public void testWriteUtf8_WF24_UnpairedLowIgnored() {
        char[] chars = {'\uDC00'};
        ByteBuf buffer = Unpooled.buffer(10);
        int written = BytesUtils.writeUtf8(buffer, chars, true);
        assertEquals(3, written); // Nessuna eccezione
    }
}