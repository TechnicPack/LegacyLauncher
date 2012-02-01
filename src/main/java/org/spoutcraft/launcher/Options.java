/*
 * This file is part of Spoutcraft Launcher (http://wiki.getspout.org/).
 * 
 * Spoutcraft Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spoutcraft Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spoutcraft.launcher;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;

public class Options {

	@Parameter
	private final List<String>	parameters	= Lists.newArrayList();
	@Parameter(names = { "-username", "-user", "-u" }, description = "Minecraft Username")
	private final String				user				= null;
	@Parameter(names = { "-password", "-pass", "-p" }, description = "Minecraft Password")
	private final String				pass				= null;
	@Parameter(names = { "-server", "-host", "-join", "-j", "-h", "-s" }, description = "Minecraft Server to join")
	private final String				server			= null;
	@Parameter(names = { "-portable", "--portable", "-pmode", "-portable_mode", "-pm" }, description = "Portable Mode")
	private final boolean				portable		= false;
	@Parameter(names = { "-safe", "-smode", "-safe_mode", "-sm" }, description = "Safe Mode - Prevents Addons from being loaded")
	private final boolean				safe_mode		= false;

	public List<String> getParameters() {
		return parameters;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}

	public String getServer() {
		if (server == null) { return null; }
		if (server.contains(":")) { return server.substring(0, server.indexOf(":")); }
		return server;
	}

	public String getPort() {
		if (server == null) { return null; }
		if (server.contains(":")) { return server.substring(server.indexOf(":") + 1); }
		return null;
	}

	public boolean isPortable() {
		return portable;
	}

	public boolean isSafe_mode() {
		return safe_mode;
	}
}