package com.BBsRs.vkaudiosync;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.DialogFragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

public class DialogActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    //showing dialog
		final DialogFragment alertDialog = new DialogFragment(){
  			private View makeNumberPicker() {
  		        View content = getLayoutInflater().inflate(
  		                R.layout.dialog_content);
  		        //do stuff with list here
  		        return content;
  		    }

  		    @Override
  		    public Dialog onCreateDialog(Bundle savedInstanceState) {
  		        AlertDialog.Builder builder = new AlertDialog.Builder(getSupportActivity(), getTheme());
  		        builder.setView(makeNumberPicker());
  		        builder.setCancelable(false);
  		        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
					}
				});
  		        return builder.create();
  		    }
  		    
  		    @Override
			public void onDismiss(DialogInterface dialogInterface) {
				finish();
			}
  		};
  		alertDialog.show(getSupportFragmentManager());
	}

}
