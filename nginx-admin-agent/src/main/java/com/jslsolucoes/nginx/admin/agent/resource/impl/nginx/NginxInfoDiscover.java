package com.jslsolucoes.nginx.admin.agent.resource.impl.nginx;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jslsolucoes.file.system.FileSystemBuilder;
import com.jslsolucoes.nginx.admin.agent.resource.impl.NginxCommandLineInterfaceResourceImpl;
import com.jslsolucoes.runtime.RuntimeResult;

@RequestScoped
public class NginxInfoDiscover {

	private static Logger logger = LoggerFactory.getLogger(NginxInfoDiscover.class);
	private NginxCommandLineInterfaceResourceImpl commandLineInterfaceResourceImpl;

	public NginxInfoDiscover() {

	}

	@Inject
	public NginxInfoDiscover(NginxCommandLineInterfaceResourceImpl commandLineInterfaceResourceImpl) {
		this.commandLineInterfaceResourceImpl = commandLineInterfaceResourceImpl;
	}

	public NginxInfo info(String nginxBin, String nginxHome) {
		NginxInfo nginxDetail = new NginxInfo();
		nginxDetail.setAddress(address());
		nginxDetail.setVersion(version(nginxBin, nginxHome));
		try {
			FileSystemBuilder.newBuilder().read().withDestination(pid(nginxHome)).withCharset("UTF-8")
					.execute(content -> {
						nginxDetail.setPid(Integer.valueOf(clear(content)));
					}).end();
			nginxDetail.setUptime(uptime(nginxHome));
		} catch (Exception ioException) {
			logger.error("Could not read pid file", ioException);
			nginxDetail.setPid(0);
			nginxDetail.setUptime(BigDecimal.ZERO);
		}
		return nginxDetail;
	}

	private static String clear(String distribution) {
		if (!StringUtils.isEmpty(distribution)) {
			return distribution.replaceAll("\n\t\r", "").trim();
		}
		return distribution;
	}

	private String address() {
		try {
			return Collections.list(NetworkInterface.getNetworkInterfaces()).stream().filter(networkInterface -> {
				try {
					return networkInterface.isUp() && !networkInterface.isLoopback();
				} catch (SocketException e) {
					return false;
				}
			}).flatMap(networkInterface -> Collections.list(networkInterface.getInetAddresses()).stream())
					.filter(inetAddress -> Inet4Address.class.isInstance(inetAddress))
					.map(inetAddress -> inetAddress.getHostAddress()).collect(Collectors.joining(","));
		} catch (SocketException socketException) {
			logger.error("Could not read network interfaces", socketException);
			return "unknow";
		}
	}

	private File pid(String nginxHome) {
		return new File(nginxHome, "nginx.pid");
	}

	private String version(String nginxBin, String nginxHome) {
		RuntimeResult runtimeResult = commandLineInterfaceResourceImpl.version(nginxBin, nginxHome);
		if (runtimeResult.isSuccess()) {
			Matcher version = Pattern.compile("([0-9]{1,}\\.[0-9]{1,}\\.[0-9]{1,})").matcher(runtimeResult.getOutput());
			if (version.find()) {
				return version.group(1);
			}
		}
		return "unknow";
	}

	public static void main(String[] args) {
		Duration duration = Duration.between(
				LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault()),
				LocalDateTime.now());
		System.out.println(duration.getSeconds());
	}

	private BigDecimal uptime(String nginxHome) {
		File pidFile = pid(nginxHome);
		Duration duration = Duration.between(
				LocalDateTime.ofInstant(Instant.ofEpochMilli(pidFile.lastModified()), ZoneId.systemDefault()),
				LocalDateTime.now());
		return BigDecimal.valueOf(duration.getSeconds())
				.divide(BigDecimal.valueOf(60), 5, RoundingMode.HALF_UP)
				.divide(BigDecimal.valueOf(60), 5, RoundingMode.HALF_UP)
				.divide(BigDecimal.valueOf(24), 5, RoundingMode.HALF_UP);
	}
}
