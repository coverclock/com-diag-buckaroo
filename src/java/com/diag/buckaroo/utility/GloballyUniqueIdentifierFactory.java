/**
 * Copyright 2007 Digital Aggregates Corp., Arvada CO 80001-0597, USA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Name$
 *
 * $Id$
 */
package com.diag.buckaroo.utility;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.PBEKeySpec;

/**
 * Generate a identifier, in the form of a String, that is guaranteed to be
 * as unique in all time and space as we know how to make it, and do so in as
 * stateless a way as possible, while revealing as little as possible to the
 * outside world, and do so in a way that is verifiable. Note that this is
 * more or less an experiment; if you want something similar but much simpler,
 * try java.util.UUID.randomUUID().toString() instead.
 */
public class GloballyUniqueIdentifierFactory {
	static final Logger logger = Logger.getLogger(GloballyUniqueIdentifierFactory.class.getPackage().getName());
	
	private final static boolean DEBUG = false; // Do NOT log debugging stuff!
	
	private final static int SLACK_VALUE = 0x00;
	private final static int BOGUS_VALUE = 0xff;
	private final static int LOW_ORDER_BYTE_MASK = 0xff;
	private final static int BITS_PER_BYTE = 8;
	
	private final static String RANDOMIZER = "SHA1PRNG";
	private final static String CHECKSUMMER = "SHA1";
	private final static String CIPHER = "PBEWithMD5AndDES";
	
	private final static int TOD_LENGTH = 8;
	private final static int IPADDR_LENGTH = 16;
	private final static int HASHCODE_LENGTH = 4;
	private final static int RANDOM_LENGTH = 8;
	private final static int CHECKSUM_LENGTH = 20;
	private final static int PLAINTEXT_LENGTH = TOD_LENGTH + IPADDR_LENGTH + HASHCODE_LENGTH + RANDOM_LENGTH + CHECKSUM_LENGTH;
	private final static int CIPHERTEXT_LENGTH = (PLAINTEXT_LENGTH + 8) * 2;
	
	private final static byte[] SALT = { (byte)0x01, (byte)0x23, (byte)0x57, (byte)0x11, (byte)0x13, (byte)0x17, (byte)0x19, (byte)0x23 };
	private final static int COUNT  = 16;
	private final static char[] PASSWORD = { 'g', 'i', 'l', 'g', 'a', 'm', 'e', 's', 'h' };

	byte[] addressbytes;
	byte[] hashcodebytes;
	
	SecureRandom randomizer;
	MessageDigest checksummer;
	SecretKey secret;
	PBEParameterSpec parameter;
	
	private static void wipe(byte[] plaintext, byte[] ciphertext) {
		for (int ii = 0; ii < plaintext.length; ++ii) { plaintext[ii] = 0; }
		if (ciphertext != plaintext) {
			for (int ii = 0; ii < ciphertext.length; ++ii) { ciphertext[ii] = 0; }
		}
	}
	
	private static String printable(byte[] anytext) {
		StringBuilder buffer = new StringBuilder(anytext.length * 2);
		for (int bits : anytext)
		{
			String hex = Integer.toHexString(bits & LOW_ORDER_BYTE_MASK);
			if (hex.length() < 2)
			{
				buffer.append('0');
			}
			buffer.append(hex);
		}
		return buffer.toString();
	}
	
	/**
	 * Ctor.
	 */
	public GloballyUniqueIdentifierFactory() {
		this(SALT, COUNT, PASSWORD);
	}
	
	/**
	 * Ctor.
	 */
	public GloballyUniqueIdentifierFactory(byte[] salt, int count, char[] password) {
		
		addressbytes = new byte[IPADDR_LENGTH];
		try	{
			InetAddress address = InetAddress.getLocalHost();
			byte[] bytes = address.getAddress();
			int aindex = 0;
			for (int ii = (addressbytes.length - bytes.length); ii > 0; --ii) {
				addressbytes[aindex++] = (byte)SLACK_VALUE;
			}
			for (byte bits : bytes) {
				addressbytes[aindex++] = bits;
			}
		}
		catch (Exception exception){
			for (int ii = 0; ii < addressbytes.length; ++ii) {
				addressbytes[ii] = (byte)BOGUS_VALUE;
			}
		}

		hashcodebytes = new byte[HASHCODE_LENGTH];
		int hashcode = System.identityHashCode(this);
		for (int ii = 0; ii < hashcodebytes.length; ++ii) {
			hashcodebytes[ii] = (byte)((hashcode >> ((HASHCODE_LENGTH - 1) * BITS_PER_BYTE)) & LOW_ORDER_BYTE_MASK);
			hashcode <<= BITS_PER_BYTE;
		}

		try {
			randomizer = SecureRandom.getInstance(RANDOMIZER);
		} catch (Exception exception) {
			logger.log(Level.WARNING, exception.toString(), exception);
			randomizer = null;
		}

		try {
			checksummer = MessageDigest.getInstance(CHECKSUMMER);
		} catch (Exception exception) {
			logger.log(Level.WARNING, exception.toString(), exception);
			checksummer = null;
		}

		try {
			parameter = new PBEParameterSpec(salt, count);
			PBEKeySpec key = new PBEKeySpec(password);
			SecretKeyFactory factory = SecretKeyFactory.getInstance(CIPHER);
			secret = factory.generateSecret(key);
		} catch (Exception exception) {
			logger.log(Level.WARNING, exception.toString(), exception);
			secret = null;
		}
	}

	/**
	 * Create an identifier that is unique in all time and space at least until
	 * the end of the UNIX Epoch at which point civilization collapses and the world
	 * as we have come to know it only has Windows PCs or IBM mainframes which will
	 * all be in the hands of those that were smart enough to stockpile ammunition.
	 * @return a character string containing the printable identifier.
	 */
	public String create()
	{
		byte[] plaintext = new byte[PLAINTEXT_LENGTH];
		byte[] ciphertext = plaintext;
		int bindex = 0;

		long tod = System.currentTimeMillis();
		for (int index = 0; index < TOD_LENGTH; ++index)
		{
			plaintext[bindex++] = (byte)((tod >> ((TOD_LENGTH - 1) * BITS_PER_BYTE)) & LOW_ORDER_BYTE_MASK);
			tod <<= BITS_PER_BYTE;
		}
		if (DEBUG) { System.err.println("tod[" + plaintext.length + "]=" + printable(plaintext)); }
		
		for (byte bits : addressbytes)
		{
			plaintext[bindex++] = bits;
		}
		if (DEBUG) { System.err.println("address[" + addressbytes.length + "]=" + printable(addressbytes)); }
		
		for (byte bits : hashcodebytes)
		{
			plaintext[bindex++] = bits;
		}
		if (DEBUG) { System.err.println("hashcode[" + hashcodebytes.length + "]=" + printable(hashcodebytes)); }
		
		if (randomizer != null)
		{
			byte randombytes[] = new byte[RANDOM_LENGTH];
		    randomizer.nextBytes(randombytes);
		    for (byte bits : randombytes)
			{
				plaintext[bindex++] = bits;
			}
			if (DEBUG) { System.err.println("random[" + randombytes.length + "]=" + printable(randombytes)); }
		}
		else
		{
			for (int index = 0; index < RANDOM_LENGTH; ++index)
			{
				plaintext[bindex++] = (byte)BOGUS_VALUE;
			}
		}
		
		if (checksummer != null)
		{
			checksummer.reset();
			checksummer.update(plaintext, 0, bindex);
			byte[] checksum = checksummer.digest();
			for (byte bits : checksum)
			{
				plaintext[bindex++] = bits;
			}
			if (DEBUG) { System.err.println("checksum[" + checksum.length + "]=" + printable(checksum)); }
		}
		else
		{
			for (int index = 0; index < CHECKSUM_LENGTH; ++index)
			{
				plaintext[bindex++] = (byte)BOGUS_VALUE;
			}			
		}
		
		if (DEBUG) { System.err.println("plaintext[" + plaintext.length + "]=" + printable(plaintext)); }
		
		if (secret != null) {
			try {
				Cipher cipher = Cipher.getInstance(CIPHER);
				cipher.init(Cipher.ENCRYPT_MODE, secret, parameter);
				ciphertext = cipher.doFinal(plaintext);
			} catch (Exception exception) {
				logger.log(Level.WARNING, exception.toString(), exception);
			}
		}
		
		if (DEBUG) { System.err.println("ciphertext[" + ciphertext.length + "]=" + printable(ciphertext)); }

		String guid = printable(ciphertext);
		wipe(plaintext, ciphertext);
		if (DEBUG) { System.err.println("guid=" + guid); }
		return guid;
	}

	/**
	 * Validate a globally unique identifier as likely to have been generated by this
	 * class and not having been tampered with.
	 * @param guid is the printable identifier.
	 * @return true if valid, false otherwise.
	 */
	public boolean isValid(String guid)
	{
		if (DEBUG) { System.err.println("guid=" + guid); }

		if ((guid == null) || (guid.length() != CIPHERTEXT_LENGTH)) {
			return false;
		}

		byte[] bytes = guid.getBytes();
		byte[] ciphertext = new byte[bytes.length / 2];
		byte[] plaintext = ciphertext;
		
		for (int ii = 0; ii < ciphertext.length; ++ii)
		{
			int datum1 = bytes[ii * 2] & LOW_ORDER_BYTE_MASK;
			if ((0x30 <= datum1) && (datum1 <= 0x39))
			{
				datum1 = datum1 - 0x30;
			}
			else if ((0x41 <= datum1) && (datum1 <= 0x46))
			{
				datum1 = datum1 - 0x37;
			}
			else if ((0x61 <= datum1) && (datum1 <= 0x66))
			{
				datum1 = datum1 - 0x57;
			}
			else
			{
				if (DEBUG) { System.err.println("datum[" + ii + "]=" + datum1); }
				wipe(plaintext, ciphertext);
				return false;
			}
			int datum2 = bytes[(ii * 2) + 1] & LOW_ORDER_BYTE_MASK;
			if ((0x30 <= datum2) && (datum2 <= 0x39))
			{
				datum2 = datum2 - 0x30;
			}
			else if ((0x41 <= datum2) && (datum2 <= 0x46))
			{
				datum2 = datum2 - 0x37;
			}
			else if ((0x61 <= datum2) && (datum2 <= 0x66))
			{
				datum2 = datum2 - 0x57;
			}
			else
			{
				wipe(plaintext, ciphertext);
				return false;
			}
			ciphertext[ii] = (byte)((datum1 << 4) + datum2);
		}
		
		if (DEBUG) { System.err.println("ciphertext[" + ciphertext.length + "]=" + printable(ciphertext)); }
		
		if (secret != null) {
			try {
				Cipher cipher = Cipher.getInstance(CIPHER);
				cipher.init(Cipher.DECRYPT_MODE, secret, parameter);
				plaintext = cipher.doFinal(ciphertext);
			} catch (Exception exception) {
				wipe(plaintext, ciphertext);
				return false;
			}
		}
		
		if (DEBUG) { System.err.println("plaintext[" + plaintext.length + "]=" + printable(plaintext)); }
		
		if (plaintext.length != PLAINTEXT_LENGTH) {
			wipe(plaintext, ciphertext);
			return false;
		}
		
		if (checksummer != null)
		{
			checksummer.reset();
			checksummer.update(plaintext, 0, PLAINTEXT_LENGTH - CHECKSUM_LENGTH);
			byte[] checksum = checksummer.digest();
			for (int ii = 0; ii < checksum.length; ++ii)
			{
				if (checksum[ii] != plaintext[PLAINTEXT_LENGTH - CHECKSUM_LENGTH + ii])
				{
					wipe(plaintext, ciphertext);
					return false;
				}
			}
		}
		
		wipe(plaintext, ciphertext);
		return true;
	}

	/**
	 * Write a GUID to standard output or validate one or more GUIDs on the command line.
	 * This is not a unit test.
	 * It is a feature.
	 */
	public static void main(String args[])
	{
		GloballyUniqueIdentifierFactory guidf = new GloballyUniqueIdentifierFactory();
		if (args.length == 0)
		{
			System.out.println(guidf.create());
		}
		else
		{
			for (String arg : args)
			{
				System.out.println(arg + "=" + guidf.isValid(arg));
			}
		}
	}
}
