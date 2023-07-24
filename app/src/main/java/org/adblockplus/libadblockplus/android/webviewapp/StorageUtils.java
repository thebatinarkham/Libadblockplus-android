package org.adblockplus.libadblockplus.android.webviewapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class StorageUtils
{
  public static void writeBundle(@NotNull final Context context, @NotNull final Bundle bundle,
                                 @NotNull final String filename)
  {
    new AsyncTask<Void, Void, Void>()
    {
      @Override
      protected Void doInBackground(final Void... voids)
      {
        FileOutputStream outputStream = null;
        try
        {
          final File outputFile = new File(context.getFilesDir(), filename);
          outputStream = new FileOutputStream(outputFile);
          final Parcel parcel = Parcel.obtain();
          parcel.writeBundle(bundle);
          outputStream.write(parcel.marshall());
          outputStream.flush();
          parcel.recycle();
        }
        catch (final Exception e)
        {
          e.printStackTrace();
        }
        finally
        {
          try
          {
            if (outputStream != null)
            {
              outputStream.close();
            }
          }
          catch (final Exception e)
          {
            e.printStackTrace();
          }
        }
        return null;
      }
    }.execute();
  }

  public static Bundle readBundle(@NotNull final Context context, @NotNull final String filename)
  {
    FileInputStream inputStream = null;
    try
    {
      final File inputFile = new File(context.getFilesDir(), filename);
      inputStream = new FileInputStream(inputFile);
      final Parcel parcel = Parcel.obtain();
      final byte[] data = new byte[(int) inputStream.getChannel().size()];
      inputStream.read(data, 0, data.length);
      parcel.unmarshall(data, 0, data.length);
      parcel.setDataPosition(0);
      final Bundle outputBundle = parcel.readBundle(ClassLoader.getSystemClassLoader());
      parcel.recycle();
      return outputBundle;
    }
    catch (final IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        if (inputStream != null)
        {
          inputStream.close();
        }
      }
      catch (final Exception e)
      {
        e.printStackTrace();
      }
    }
    return null;
  }

  public static void deleteBundle(@NotNull final Context context, @NotNull final String filename)
  {
    try
    {
      final File inputFile = new File(context.getFilesDir(), filename);
      inputFile.delete();
    }
    catch (final Exception e)
    {
      e.printStackTrace();
    }
  }
}

