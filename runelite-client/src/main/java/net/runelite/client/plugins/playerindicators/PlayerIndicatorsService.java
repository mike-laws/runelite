/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.playerindicators;

import java.awt.Color;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Player;

@Singleton
public class PlayerIndicatorsService
{
	private final Client client;
	private final PlayerIndicatorsConfig config;

	@Inject
	private PlayerIndicatorsService(Client client, PlayerIndicatorsConfig config)
	{
		this.config = config;
		this.client = client;
	}
	private int interpolate(int levelLow, int levelHigh, int colorLow, int colorHigh, int level)
	{
		double percentage = (double)(level - levelLow) / (levelHigh - levelLow);
		return (int)(colorLow + (percentage * (colorHigh - colorLow)));
	}
	private int[] colorTable = {
			-15, 0x00, 0xff, 0x00,
			0, 0xff, 0xA5, 0x00,
			15, 0xff, 0x00, 0x00
	};

	private Color getColor(int levelDifference)
	{

		if(levelDifference < 0)
		{
			int low = colorTable[0];
			int high = colorTable[4];
			return new Color(interpolate(low, high, colorTable[1], colorTable[1 + 4],levelDifference),
					interpolate(low, high, colorTable[2], colorTable[2 + 4],levelDifference),
					interpolate(low, high, colorTable[3], colorTable[3 + 4],levelDifference));
		}

		int low = colorTable[4];
		int high = colorTable[8];
		return new Color(interpolate(low, high, colorTable[1 + 4], colorTable[1 + 8],levelDifference),
				interpolate(low, high, colorTable[2 + 4], colorTable[2 + 8],levelDifference),
				interpolate(low, high, colorTable[3 + 4], colorTable[3 + 8],levelDifference));
	}


	public void forEachPlayer(final BiConsumer<Player, Color> consumer)
	{
		Color color = null;


		int levelLow = client.getLocalPlayer().getCombatLevel() - 15;
		int levelHigh = client.getLocalPlayer().getCombatLevel() + 15;

		final Player localPlayer = client.getLocalPlayer();

		for (Player player : client.getPlayers()) {
			if (player == null || player.getName() == null) {
				continue;
			}
			if(player == localPlayer) continue;
			if(player.getCombatLevel() < levelLow || player.getCombatLevel() > levelHigh) continue;

			int levelDifference = player.getCombatLevel() - client.getLocalPlayer().getCombatLevel();
			color = getColor(levelDifference);
			consumer.accept(player, color);
		}


		return;
		/*
		if (!config.highlightOwnPlayer() && !config.drawFriendsChatMemberNames()
			&& !config.highlightFriends() && !config.highlightOthers())
		{
			return;
		}

		final Player localPlayer = client.getLocalPlayer();

		for (Player player : client.getPlayers())
		{
			if (player == null || player.getName() == null)
			{
				continue;
			}

			boolean isFriendsChatMember = player.isFriendsChatMember();

			if (player == localPlayer)
			{
				if (config.highlightOwnPlayer())
				{
					consumer.accept(player, config.getOwnPlayerColor());
				}
			}
			else if (config.highlightFriends() && player.isFriend())
			{
				consumer.accept(player, config.getFriendColor());
			}
			else if (config.drawFriendsChatMemberNames() && isFriendsChatMember)
			{
				consumer.accept(player, config.getFriendsChatMemberColor());
			}
			else if (config.highlightTeamMembers() && localPlayer.getTeam() > 0 && localPlayer.getTeam() == player.getTeam())
			{
				consumer.accept(player, config.getTeamMemberColor());
			}
			else if (config.highlightOthers() && !isFriendsChatMember)
			{
				consumer.accept(player, config.getOthersColor());
			}
		}
		 */
	}
}
