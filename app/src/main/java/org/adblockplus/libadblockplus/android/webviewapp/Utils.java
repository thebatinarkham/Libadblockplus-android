package org.adblockplus.libadblockplus.android.webviewapp;

import android.view.View;
import android.view.inputmethod.InputMethodManager;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class Utils
{
  public static void hideSoftwareKeyboard(final View view)
  {
    final InputMethodManager imm = (InputMethodManager)view.getContext()
        .getSystemService(INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }
}
