

package org.adblockplus.libadblockplus.android.webviewapp;


import org.adblockplus.libadblockplus.BuildConfig;
import org.adblockplus.libadblockplus.R;
import org.adblockplus.libadblockplus.android.AdblockEngine;
import org.adblockplus.libadblockplus.android.AdblockEngineProvider;
import org.adblockplus.libadblockplus.android.settings.AdblockHelper;

import timber.log.Timber;

public class Application extends android.app.Application
{
  private final AdblockEngineProvider.EngineCreatedListener engineCreatedListener =
    new AdblockEngineProvider.EngineCreatedListener()
  {
    @Override
    public void onAdblockEngineCreated(final AdblockEngine adblockEngine)
    {
      // put your AdblockEngine initialization here
    }
  };

  private final AdblockEngineProvider.EngineDisposedListener engineDisposedListener =
    new AdblockEngineProvider.EngineDisposedListener()
  {
    @Override
    public void onAdblockEngineDisposed()
    {
      // put your AdblockEngine de-initialization here
    }
  };

  @Override
  public void onCreate()
  {
    super.onCreate();

    if (BuildConfig.DEBUG)
    {
      Timber.plant(new Timber.DebugTree());
    }

    // it's not initialized here but we check it just to show API usage
    if (!AdblockHelper.get().isInit())
    {

      final AdblockHelper helper = AdblockHelper.get();
      helper
        .init(this, null /*use default value*/, AdblockHelper.PREFERENCE_NAME)
        .preloadSubscriptions(
                R.raw.easylist_minified,
                R.raw.exceptionrules_minimal)
        .addEngineCreatedListener(engineCreatedListener)
        .addEngineDisposedListener(engineDisposedListener);

      if (!BuildConfig.ADBLOCK_ENABLED)
      {
        Timber.d("onCreate() with DisabledByDefault");
        helper.setDisabledByDefault();
      }

      helper.getSiteKeysConfiguration().setForceChecks(true);
    }
  }
}
