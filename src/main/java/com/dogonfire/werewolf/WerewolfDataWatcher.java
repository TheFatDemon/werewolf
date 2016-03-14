package com.dogonfire.werewolf;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.server.v1_9_R1.DataWatcherObject;
import org.apache.commons.lang3.ObjectUtils;

import net.minecraft.server.v1_9_R1.DataWatcher;
import net.minecraft.server.v1_9_R1.Entity;

public class WerewolfDataWatcher extends DataWatcher
{
	static Method	cMethod;
	static Field	eBoolean;

	static
	{
		try
		{
			cMethod = DataWatcher.class.getDeclaredMethod("c", new Class[]{DataWatcherObject.class});
			cMethod.setAccessible(true);
		}
		catch (Exception localException1)
		{
		}
		
		try
		{
			eBoolean = DataWatcher.class.getDeclaredField("e");
			eBoolean.setAccessible(true);
		}
		catch (Exception localException2)
		{
		}
	}

	public WerewolfDataWatcher(Entity arg0)
	{
		super(arg0);
	}

	public void set(DataWatcherObject datawatcherobject, Object t0)
	{
		DataWatcher.Item datawatcher_item = getItem(datawatcherobject);

		if (ObjectUtils.notEqual(t0, datawatcher_item.b())) {
			datawatcher_item.a(t0);
			datawatcher_item.a(true);

			try {
				eBoolean.setBoolean(this, true);
			} catch (Exception ignored) {
			}
		}
	}

    public DataWatcher.Item getItem(DataWatcherObject object) {
        try
        {
            return (DataWatcher.Item) cMethod.invoke(this, new Object[]{object});
        }
        catch (IllegalAccessException| InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
