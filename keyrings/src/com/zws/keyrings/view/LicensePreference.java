package com.zws.keyrings.view;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class LicensePreference extends DialogPreference 
{
    public LicensePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);     
    }
    
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		builder.setPositiveButton(null, null);
		builder.setNegativeButton(null, null);
		super.onPrepareDialogBuilder(builder);  
    }
}