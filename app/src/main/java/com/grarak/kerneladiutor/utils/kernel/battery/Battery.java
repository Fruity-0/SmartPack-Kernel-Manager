/*
 * Copyright (C) 2015-2016 Willi Ye <williye97@gmail.com>
 *
 * This file is part of Kernel Adiutor.
 *
 * Kernel Adiutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kernel Adiutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kernel Adiutor.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.grarak.kerneladiutor.utils.kernel.battery;

import android.content.Context;
import android.text.TextUtils;
 
import com.grarak.kerneladiutor.R;
import android.support.annotation.NonNull;

import com.grarak.kerneladiutor.fragments.ApplyOnBootFragment;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.Control;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by willi on 26.06.16.
 */
public class Battery {

    private static Battery sInstance;

    public static Battery getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new Battery(context);
        }
        return sInstance;
    }

    private static final String FAST_CHARGE = "/sys/kernel/fast_charge";
    private static final String FORCE_FAST_CHARGE = FAST_CHARGE + "/force_fast_charge";
    private static final String CUSTOM_AC_CHARGE_LEVEL = FAST_CHARGE + "/ac_charge_level";
    private static final String CUSTOM_USB_CHARGE_LEVEL = FAST_CHARGE + "/usb_charge_level";
    private static final String CUSTOM_WIRELESS_CHARGE_LEVEL = FAST_CHARGE + "/wireless_charge_level";

    private static final String MTP_FORCE_FAST_CHARGE = FAST_CHARGE + "/use_mtp_during_fast_charge";
    private static final String SCREEN_ON_CURRENT_LIMT = FAST_CHARGE + "/screen_on_current_limit";

    private static final String AC_CHARGE_LEVEL = FAST_CHARGE + "/ac_levels";
    private static final String USB_CHARGE_LEVEL = FAST_CHARGE + "/usb_levels";
    private static final String WIRELESS_CHARGE_LEVEL = FAST_CHARGE + "/wireless_levels";
    private static final String FAILSAFE_CONTROL = FAST_CHARGE + "/failsafe";

    private static final String CHARGE_LEVEL = "/sys/kernel/charge_levels";
    private static final String CHARGE_LEVEL_AC = CHARGE_LEVEL + "/charge_level_ac";
    private static final String CHARGE_LEVEL_USB = CHARGE_LEVEL + "/charge_level_usb";
    private static final String CHARGE_LEVEL_WL = CHARGE_LEVEL + "/charge_level_wireless";
    private static final String CHARGE_INFO = CHARGE_LEVEL + "/charge_info";

    private static final String BLX = "/sys/devices/virtual/misc/batterylifeextender/charging_limit";

    private static final String CHARGING_CURRENT = "/sys/class/power_supply/battery/current_now";
    private static final String CHARGE_STATUS = "/sys/class/power_supply/battery/status";
    private static final String CHARGE_SOURCE = "/sys/class/power_supply/battery/batt_charging_source";
    private static final String CHARGE_TYPE = "/sys/class/power_supply/usb/type";

    private static final String BCL = "/sys/class/power_supply/battery/batt_slate_mode";

    private int mCapacity;
    private static String[] sBatteryAvailable;
    private static String[] sBatteryUSBAvailable;
    private static String[] sBatteryWIRELESSAvailable;

    private Battery(Context context) {
        if (mCapacity == 0) {
            try {
                Class<?> powerProfile = Class.forName("com.android.internal.os.PowerProfile");
                Constructor constructor = powerProfile.getDeclaredConstructor(Context.class);
                Object powerProInstance = constructor.newInstance(context);
                Method batteryCap = powerProfile.getMethod("getBatteryCapacity");
                mCapacity = Math.round((long) (double) batteryCap.invoke(powerProInstance));
            } catch (Exception e) {
                e.printStackTrace();
                mCapacity = 0;
            }
        }
    }

    public void setBlx(int value, Context context) {
        run(Control.write(String.valueOf(value == 0 ? 101 : value - 1), BLX), BLX, context);
    }

    public int getBlx() {
        int value = Utils.strToInt(Utils.readFile(BLX));
        return value > 100 ? 0 : value + 1;
    }

    public boolean hasBlx() {
        return Utils.existFile(BLX);
    }

    public boolean hasbatterychargelimit() {
        return Utils.existFile(BCL);
    }

    public static String getbatterychargelimit() {
        return Utils.readFile(BCL);
    }

    public void enablebatterychargelimit(boolean enable, Context context) {
        run(Control.write(enable ? "0" : "1", BCL), BCL, context);
    }

    public boolean batterychargelimitenabled() {
        return Utils.readFile(BCL).equals("0");
    }

    public static List<String> enableForceFastCharge(Context context) {
        List<String> list = new ArrayList<>();
        list.add(context.getString(R.string.disabled));
        list.add(context.getString(R.string.enabled));
        list.add(context.getString(R.string.custom_charge));
        return list;
    }

    public static boolean hasForceFastCharge() {
        return Utils.existFile(FORCE_FAST_CHARGE);
    }

    public void ForceFastChargeenable(boolean enable, Context context) {
        run(Control.write(enable ? "1" : "0", FORCE_FAST_CHARGE), FORCE_FAST_CHARGE, context);
    }

    public boolean isForceFastChargeEnabled() {
        return Utils.readFile(FORCE_FAST_CHARGE).equals("1");
    }
    
    public static int getForceFastCharge() {
        return Utils.strToInt(Utils.readFile(FORCE_FAST_CHARGE));
    }
    
    public void setForceFastCharge(int value, Context context) {
        run(Control.write(String.valueOf(value), FORCE_FAST_CHARGE), FORCE_FAST_CHARGE, context);
    }

    public boolean iscustomodenabled() {
        return Utils.readFile(FORCE_FAST_CHARGE).equals("2");
    }

    public static boolean hasFastChargeControlAC() {
        return Utils.existFile(AC_CHARGE_LEVEL);
    }  
   
    public static String getFastChargeCustomAC() {
        return Utils.readFile(CUSTOM_AC_CHARGE_LEVEL);
    }

    public void setFastChargeControlAC (String value, Context context) {
        run(Control.write(String.valueOf(value), CUSTOM_AC_CHARGE_LEVEL), CUSTOM_AC_CHARGE_LEVEL, context);
    }
    
    public static List<String> getFastChargeControlAC() {
        if (sBatteryAvailable == null) {
            sBatteryAvailable = Utils.readFile(AC_CHARGE_LEVEL).split(" ");
        }
        return new ArrayList<>(Arrays.asList(sBatteryAvailable));
    }

    public static boolean hasFastChargeControlUSB() {
       return Utils.existFile(USB_CHARGE_LEVEL);
    }
   
    public static String getFastChargeCustomUSB() {
        return Utils.readFile(CUSTOM_USB_CHARGE_LEVEL);
    }
    
    public static List<String> getFastChargeControlUSB() {
        if (sBatteryUSBAvailable == null) {
            sBatteryUSBAvailable = Utils.readFile(USB_CHARGE_LEVEL).split(" ");
        }
        return new ArrayList<>(Arrays.asList(sBatteryUSBAvailable));
    }
    
    public void setFastChargeControlUSB (String value, Context context) {
        run(Control.write(String.valueOf(value), CUSTOM_USB_CHARGE_LEVEL), CUSTOM_USB_CHARGE_LEVEL, context);
    }
    
    public boolean hasFastChargeControlWIRELESS() {
        return Utils.existFile(WIRELESS_CHARGE_LEVEL);
    }
    
    public static String getFastChargeCustomWIRELESS() {
        return Utils.readFile(CUSTOM_WIRELESS_CHARGE_LEVEL);
    }
    
    public static List<String> getFastChargeControlWIRELESS() {
        if (sBatteryWIRELESSAvailable == null) {
            sBatteryWIRELESSAvailable = Utils.readFile(WIRELESS_CHARGE_LEVEL).split(" ");
        }
        return new ArrayList<>(Arrays.asList(sBatteryWIRELESSAvailable));
    }
    
    public void setFastChargeControlWIRELESS (String value, Context context) {
        run(Control.write(String.valueOf(value), CUSTOM_WIRELESS_CHARGE_LEVEL), CUSTOM_WIRELESS_CHARGE_LEVEL, context);
    }
    
    public void enableMtpForceFastCharge(boolean enable, Context context) {
        run(Control.write(enable ? "1" : "0", MTP_FORCE_FAST_CHARGE), MTP_FORCE_FAST_CHARGE, context);
    }

    public boolean isMtpForceFastChargeEnabled() {
        return Utils.readFile(MTP_FORCE_FAST_CHARGE).equals("1");
    }

    public boolean hasMtpForceFastCharge() {
       return Utils.existFile(MTP_FORCE_FAST_CHARGE);
    }
    
    public void enableScreenCurrentLimit(boolean enable, Context context) {
        run(Control.write(enable ? "1" : "0", SCREEN_ON_CURRENT_LIMT), SCREEN_ON_CURRENT_LIMT, context);
    }

    public boolean isScreenCurrentLimit() {
        return Utils.readFile(SCREEN_ON_CURRENT_LIMT).equals("1");
    }

    public boolean hasScreenCurrentLimit() {
        return Utils.existFile(SCREEN_ON_CURRENT_LIMT);
    }

    public static boolean haschargingstatus() {
        return Utils.existFile(CHARGING_CURRENT);
    }
    
    public static int getchargingstatus() {
        return Utils.strToInt(Utils.readFile(CHARGING_CURRENT));
    }
    
    public static boolean isDischarging() {
        return Utils.readFile(CHARGE_STATUS).equals("Discharging");
    }

    public static int ChargingType() {
        return Utils.strToInt(Utils.readFile(CHARGE_SOURCE));
    }

    public static boolean isDASHCharging() {
        return Utils.readFile(CHARGE_TYPE).equals("DASH");
    }

    public static boolean isACCharging() {
        return Utils.readFile(CHARGE_TYPE).equals("USB_DCP");
    }

    public static boolean isUSBCharging() {
        return Utils.readFile(CHARGE_TYPE).equals("USB");
    }

    public void setchargeLevelAC(int value, Context context) {
        run(Control.write(String.valueOf(value), CHARGE_LEVEL_AC), CHARGE_LEVEL_AC, context);
    }

    public static int getchargeLevelAC() {
        return Utils.strToInt(Utils.readFile(CHARGE_LEVEL_AC));
    }

    public static boolean haschargeLevelAC() {
        return Utils.existFile(CHARGE_LEVEL_AC);
    }

    public void setchargeLevelUSB(int value, Context context) {
        run(Control.write(String.valueOf(value), CHARGE_LEVEL_USB), CHARGE_LEVEL_USB, context);
    }

    public static int getchargeLevelUSB() {
        return Utils.strToInt(Utils.readFile(CHARGE_LEVEL_USB));
    }

    public static boolean haschargeLevelUSB() {
        return Utils.existFile(CHARGE_LEVEL_USB);
    }

    public void setchargeLevelWL(int value, Context context) {
        run(Control.write(String.valueOf(value), CHARGE_LEVEL_WL), CHARGE_LEVEL_WL, context);
    }

    public static int getchargeLevelWL() {
        return Utils.strToInt(Utils.readFile(CHARGE_LEVEL_WL));
    }

    public static boolean haschargeLevelWL() {
        return Utils.existFile(CHARGE_LEVEL_WL);
    }

    public static int getchargeInfo() {
        return Utils.strToInt(Utils.readFile(CHARGE_INFO));
    }

    public static boolean haschargeInfo() {
        return Utils.existFile(CHARGE_INFO);
    }

    public int getCapacity() {
        return mCapacity;
    }

    public boolean hasCapacity() {
        return getCapacity() != 0;
    }

    public boolean supported() {
        return hasCapacity();
    }

    private void run(String command, String id, Context context) {
        Control.runSetting(command, ApplyOnBootFragment.BATTERY, id, context);
    }

}
