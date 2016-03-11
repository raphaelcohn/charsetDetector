// The MIT License (MIT)
//
// Copyright © 2016, Raphael Cohn <raphael.cohn@stormmq.com>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.stormmq.charsetDetector;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.mozilla.intl.chardet.*;

import java.nio.charset.Charset;

import static java.nio.charset.Charset.forName;
import static org.mozilla.intl.chardet.nsPSMDetector.ALL;

public final class CharsetDetector
{
	@NotNull private static final Charset Utf8Charset = forName("UTF-8");
	@NotNull private static final Charset AsciiCharset = forName("US-ASCII");

	private static final int MaximumBytesToUseToDetectCharacterSet = 1024;

	@NotNull
	public static Charset detectCharset(final boolean preferUtf8IfAsciiDetected, @NotNull final byte... code)
	{
		@NonNls final Charset[] charsetDetectedHack = {AsciiCharset};
		final nsDetector nsDetector = new nsDetector(ALL);
		nsDetector.Init(new nsICharsetDetectionObserver()
		{
			@Override
			public void Notify(@NotNull @NonNls final String charset)
			{
				charsetDetectedHack[0] = forName(charset);
			}
		});

		final int length = code.length;
		final int bytesToUseToDetectCharacterSet = length > MaximumBytesToUseToDetectCharacterSet ? MaximumBytesToUseToDetectCharacterSet : length;

		if (nsDetector.isAscii(code, bytesToUseToDetectCharacterSet))
		{
			if (preferUtf8IfAsciiDetected)
			{
				return Utf8Charset;
			}
			return AsciiCharset;
		}
		else
		{
			// There is a slight possibility that the amount of data to read is too short and so Notify() isn't called
			nsDetector.DoIt(code, bytesToUseToDetectCharacterSet, false);
			nsDetector.DataEnd();
			final Charset charset = charsetDetectedHack[0];
			if (charset.equals(AsciiCharset))
			{
				if (preferUtf8IfAsciiDetected)
				{
					return Utf8Charset;
				}
			}
			return charset;
		}
	}

	private CharsetDetector()
	{
	}
}
