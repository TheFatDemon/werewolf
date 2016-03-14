package com.dogonfire.werewolf;

import java.lang.reflect.Field;
import java.util.UUID;

import com.dogonfire.werewolf.ClanManager.ClanType;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_9_R1.*;
//import net.minecraft.server.v1_9_R1.EnumPlayerInfoAction;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
//import net.minecraft.server.v1_8_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
//import net.minecraft.server.v1_8_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
//import net.minecraft.server.v1_8_R2.PacketPlayOutEntityLook;
import net.minecraft.server.v1_9_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
//import net.minecraft.server.v1_8_R2.PacketPlayOutRelEntityMoveLook;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class WerewolfSkin
{
	private UUID		accountUUID;
	private int 		entityID;
	private String		accountName;
	private String 		texture;
	private String 		textureSignature;
	private int			encposX;
	private int			encposY;
	private int			encposZ;
	private boolean		firstSpawnPacket	= false;
	private WerewolfDataWatcher	metadata;
	private boolean		burning				= false;
	private byte		hasCustomName		= 1;
	private WerewolfEntity werewolfEntity = null;

	WerewolfSkin(String accountName, UUID accountId, String texture, String textureSignature, int id)
	{
		this.entityID = id;
		
		 // TODO: Replaced with SkinCache.getPlayerUUIDFor
		this.accountUUID = UUID.randomUUID(); //UUID.nameUUIDFromBytes(new byte[] { (byte)(int)(Math.random() * 255.0D), (byte)(int)(Math.random() * 255.0D), (byte)(int)(Math.random() * 255.0D) });
		this.accountName = accountName;

		this.texture = texture;
		this.textureSignature = textureSignature;
		
		this.metadata = new WerewolfDataWatcher(null);
		this.metadata.register(wrapByte(0), (byte) 0);
		this.metadata.register(wrapString(5), accountName);

		this.metadata.register(wrapInt(12), 0);

		setCustomName(true);
	}

	public static byte degreeToByte(float degree)
	{
        return (byte) ((int) (degree * 256.0F / 360.0F));
	}

	public int getEntityID()
	{
		return this.entityID;
	}

	public void setCrouch(boolean crouched)
	{
		if (crouched)
		{
			this.metadata.set(wrapByte(0), (byte) 2);
		}
		else
		{
			this.metadata.set(wrapByte(0), (byte) 0);
		}
	}

	public void setBurning(boolean burning)
	{
		if (burning)
		{
			this.metadata.set(wrapByte(0), (byte) 1);
		}
		else
		{
			this.metadata.set(wrapByte(0), (byte) 0);
		}
	}

	public void setCustomName(boolean visible)
	{
	}

	public PacketPlayOutEntityMetadata getMetadataPacket()
	{
		return new PacketPlayOutEntityMetadata(this.entityID, this.metadata, true);
	}

	public PacketPlayOutNamedEntitySpawn getPlayerSpawnPacket(Location loc, int item)
	{
		PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();

		int x = MathHelper.floor(loc.getX() * 32.0D);
		int y = MathHelper.floor(loc.getY() * 32.0D);
		int z = MathHelper.floor(loc.getZ() * 32.0D);
		if (!this.firstSpawnPacket)
		{
			this.encposX = x;
			this.encposY = y;
			this.encposZ = z;

			this.firstSpawnPacket = true;
		}
		try
		{
			Field idField = packet.getClass().getDeclaredField("a");
			Field UUIDField = packet.getClass().getDeclaredField("b");
			Field xField = packet.getClass().getDeclaredField("c");
			Field yField = packet.getClass().getDeclaredField("d");
			Field zField = packet.getClass().getDeclaredField("e");
			Field yawField = packet.getClass().getDeclaredField("f");
			Field pitchField = packet.getClass().getDeclaredField("g");
			Field metadataField = packet.getClass().getDeclaredField("h");
            Field itemsField = packet.getClass().getDeclaredField("i");

			idField.setAccessible(true);
			UUIDField.setAccessible(true);
			xField.setAccessible(true);
			yField.setAccessible(true);
			zField.setAccessible(true);
			yawField.setAccessible(true);
			pitchField.setAccessible(true);
            metadataField.setAccessible(true);
			itemsField.setAccessible(true);

			idField.set(packet, this.entityID);
			UUIDField.set(packet, this.accountUUID);
			xField.set(packet, (double) x);
			yField.set(packet, (double) y);
			zField.set(packet, (double) z);
			yawField.set(packet, degreeToByte(loc.getYaw()));
			pitchField.set(packet, degreeToByte(loc.getPitch()));
			metadataField.set(packet, this.metadata);
            itemsField.set(packet, Lists.newArrayList(this.metadata.getItem(wrapInt(item)))); // FIXME
		}
		catch (Exception e)
		{
			System.out.println("Werewolf could not access a PacketPlayOutNamedEntitySpawn package!");
			e.printStackTrace();
		}
		return packet;
	}

	public PacketPlayOutEntityDestroy getEntityDestroyPacket()
	{
		return new PacketPlayOutEntityDestroy(new int[] { this.entityID });
	}

	public PacketPlayOutEntityEquipment getEquipmentChangePacket(short slot, ItemStack item)
	{
		PacketPlayOutEntityEquipment packet;

		if (item == null)
		{
			packet = new PacketPlayOutEntityEquipment();

			try
			{
				{
					Field field = packet.getClass().getDeclaredField("a");
					field.setAccessible(true);
					field.setInt(packet, this.entityID);
				}

				{
					Field field = packet.getClass().getDeclaredField("b");
					field.setAccessible(true);
					field.setShort(packet, slot);
				}

				{
					Field itemField = packet.getClass().getDeclaredField("c");
					itemField.setAccessible(true);
					itemField.set(packet, null);
				}
			}
			catch (Exception ex)
			{
				System.out.println("Werewolf was unable to access a PacketPlayOutEntityEquipment field!");
				ex.printStackTrace();
			}
		}
		else
		{
			packet = new PacketPlayOutEntityEquipment(this.entityID, EnumItemSlot.MAINHAND, item);
		}

		return packet;
	}

	public PacketPlayOutEntityLook getEntityLookPacket(Location loc)
	{
		return new PacketPlayOutEntityLook(this.entityID, degreeToByte(loc.getYaw()), degreeToByte(loc.getPitch()), true);
	}

	public byte[] getYawPitch(Location loc)
	{
		byte yaw = degreeToByte(loc.getYaw());
		byte pitch = degreeToByte(loc.getPitch());
		return new byte[] { yaw, pitch };
	}

	public PacketPlayOutRelEntityMoveLook getEntityMoveLookPacket(Location look)
	{
		byte[] yp = getYawPitch(look);

		MovementValues movement = getMovement(look);

		this.encposX += movement.x;
		this.encposY += movement.y;
		this.encposZ += movement.z;

		return new PacketPlayOutRelEntityMoveLook(this.entityID, (byte) movement.x, (byte) movement.y, (byte) movement.z, yp[0], yp[1], false);
	}

	public MovementValues getMovement(Location to)
	{
		int x = MathHelper.floor(to.getX() * 32.0D);
		int y = MathHelper.floor(to.getY() * 32.0D);
		int z = MathHelper.floor(to.getZ() * 32.0D);

		int diffx = x - this.encposX;
		int diffy = y - this.encposY;
		int diffz = z - this.encposZ;

		return new MovementValues(diffx, diffy, diffz, degreeToByte(to.getYaw()), degreeToByte(to.getPitch()));
	}

	public PacketPlayOutEntityTeleport getEntityTeleportPacket(Location loc)
	{
		int x = MathHelper.floor(32.0D * loc.getX());
		int y = MathHelper.floor(32.0D * loc.getY());
		int z = MathHelper.floor(32.0D * loc.getZ());

		PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();

		try
		{
			{
				Field field = packet.getClass().getDeclaredField("a");
				field.setAccessible(true);
				field.setInt(packet, this.entityID);
				//field.setAccessible(!field.isAccessible());
			}

			{
				Field field = packet.getClass().getDeclaredField("b");
				field.setAccessible(true);
				field.setInt(packet, x);
				//field.setAccessible(!field.isAccessible());
			}

			{
				Field field = packet.getClass().getDeclaredField("c");
				field.setAccessible(true);
				field.setInt(packet, y);
			}

			{
				Field field = packet.getClass().getDeclaredField("d");
				field.setAccessible(true);
				field.setInt(packet, z);
			}

			{
				Field field = packet.getClass().getDeclaredField("e");
				field.setAccessible(true);
				field.setByte(packet, degreeToByte(loc.getYaw()));
			}

			{
				Field field = packet.getClass().getDeclaredField("f");
				field.setAccessible(true);
				field.setByte(packet, degreeToByte(loc.getPitch()));
			}
		}
		catch (Exception ex)
		{
			System.out.println("Werewolf was unable to access a PacketPlayOutEntityTeleport field!");
			ex.printStackTrace();
		}

		if (!this.firstSpawnPacket)
		{
			this.encposX = x;
			this.encposY = y;
			this.encposZ = z;
		}
		
		return packet;
	}

	public PacketPlayOutPlayerInfo getPlayerInfoPacket()
	{
		return getPlayerInfoPacket(null, false);
	}
	
	public PacketPlayOutPlayerInfo getPlayerInfoPacket(Player player, boolean show)
	{
	    EntityPlayer playerEntity = ((CraftPlayer)player).getHandle();
	    GameProfile gameProfile = new GameProfile(this.accountUUID, "Werewolf");

	    gameProfile.getProperties().put("textures", new Property("textures", texture, textureSignature));
	
		FieldSetter.b(GameProfile.class, gameProfile, "id", this.accountUUID);
		FieldSetter.b(GameProfile.class, gameProfile, "name", "Werewolf");
	    
	    if (show) 
	    {
	    	this.werewolfEntity = WerewolfEntity.newWerewolfEntity(player.getLocation(), gameProfile, playerEntity.playerConnection);

	    	return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, this.werewolfEntity);
	    }
	    else
	    {
		    return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, this.werewolfEntity);
	    }
	}

	public PacketPlayOutEntityHeadRotation getHeadRotatePacket(Location loc)
	{
		PacketPlayOutEntityHeadRotation packet = new PacketPlayOutEntityHeadRotation();
		try
		{
			{
				Field field = packet.getClass().getDeclaredField("a");
				field.setAccessible(true);
				field.setInt(packet, this.entityID);
			}

			{
				Field field = packet.getClass().getDeclaredField("b");
				field.setAccessible(true);
				field.setByte(packet, degreeToByte(loc.getYaw()));
			}
		}
		catch (Exception ex)
		{
			System.out.println("Werewolf was unable to access a PacketPlayOutEntityHeadRotation field!");
			ex.printStackTrace();
		}
		return packet;
	}

	public PacketPlayOutAnimation getAnimationPacket(int animation)
	{
		PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
		try
		{
			Field idField = packet.getClass().getDeclaredField("a");
			Field animationField = packet.getClass().getDeclaredField("b");

			idField.setAccessible(true);
			animationField.setAccessible(true);
			idField.set(packet, this.entityID);
			animationField.set(packet, animation);
		}
		catch (Exception ex)
		{
			System.out.println("Werewolf was unable to access a PacketPlayOutAnimation field!");
			ex.printStackTrace();
		}
		return packet;
	}

	public PacketPlayOutScoreboardTeam getScoreBoardTeamPacket(Team team)
	{
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();

		return packet;
	}

	private DataWatcherObject wrapInt(int val)
	{
		return new DataWatcherObject<>(val, DataWatcherRegistry.b);
	}

    private DataWatcherObject wrapByte(int val)
    {
        return new DataWatcherObject<>(val, DataWatcherRegistry.a);
    }

    private DataWatcherObject wrapString(int val)
    {
        return new DataWatcherObject<>(val, DataWatcherRegistry.d);
    }
}
