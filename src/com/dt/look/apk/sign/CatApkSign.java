/**
 * CatApkSign.java Created on 2015-12-1
 */
package com.dt.look.apk.sign;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The class <code>CatApkSign</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class CatApkSign {

	private static final String CMD = "keytool -printcert -file %s";

	public static void main(String[] args) {

		if (args == null || args.length == 0) {
			System.err.println("Usage:<apk>");
			return;
		}

		try {
			final String dir = getTmpDir();
			final String apk = args[0];
			if (null == dir || !new File(dir).exists()) {
				System.err.println("系统临时文件目录不存在!");
			}
			final String rsaFile = dir + File.separator
					+ System.currentTimeMillis() + "CERT.RSA";
			System.out.println(apk);
			unzipRSA(apk, rsaFile);
			catSign(rsaFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("错误!");
		}
		final Scanner scanner = new Scanner(System.in);
		System.out.println("按任意键继续...");
		scanner.nextLine();
	}

	private static String getTmpDir() {
		return System.getProperty("java.io.tmpdir");
	}

	private static boolean unzipRSA(String file, String destFile) throws Exception {
		try {
			final ZipInputStream zins = new ZipInputStream(new FileInputStream(
					file));
			ZipEntry ze = null;
			final byte[] buffer = new byte[256];
			final FileOutputStream out = new FileOutputStream(destFile);
			while ((ze = zins.getNextEntry()) != null) {
				if (ze.getName().matches("META-INF/\\w*.RSA")) {
					int len = -1;
					while (-1 != (len = zins.read(buffer))) {
						out.write(buffer, 0, len);
					}
					out.flush();
					out.close();
					zins.close();
					return true;
				}
			}
		} catch (Exception e) {
			System.err.println("Apk文件有问题");
			throw e;
		}
		return false;
	}

	private static boolean catSign(String path) throws Exception {
		try {
			String coding = System.getProperty("sun.jnu.encoding", "GBK");
			final String cmd = String.format(CMD, path);
			Process exec = Runtime.getRuntime().exec(cmd);

			exec.waitFor();
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(exec.getInputStream(), coding));
			String line = null;
			while (null != (line = reader.readLine())) {
				if (line.contains("MD5") || line.contains("md5")) {
					line = line.replace("MD5:", "");
					line = line.replace(":", "");
					System.out.println("签名为：");
					System.out.println(line.trim().toLowerCase());
				}
			}
			if (exec.exitValue() == 0) {
				System.out.println(String.format("读取签名成功"));
				return true;
			} else {
				throw new Exception("读取签名错误");
			}
		} catch (Exception e) {
			throw new Exception("读取签名错误");
		}
	}
}
