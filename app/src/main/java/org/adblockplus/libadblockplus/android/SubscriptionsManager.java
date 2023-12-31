package org.adblockplus.libadblockplus.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;;
import org.adblockplus.libadblockplus.FilterEngine;
import org.adblockplus.libadblockplus.Subscription;
import org.adblockplus.libadblockplus.android.settings.AdblockHelper;

import java.util.List;

import timber.log.Timber;

public class SubscriptionsManager
{
  private static final String ACTION_PREFIX =
    "org.adblockplus.libadblockplus.android.intent.action.SUBSCRIPTION_";
  private static final String ACTION_LIST = ACTION_PREFIX + "LIST";
  private static final String ACTION_ADD = ACTION_PREFIX + "ADD";
  private static final String ACTION_ENABLE = ACTION_PREFIX + "ENABLE";
  private static final String ACTION_DISABLE = ACTION_PREFIX + "DISABLE";
  private static final String ACTION_REMOVE = ACTION_PREFIX + "REMOVE";
  private static final String ACTION_UPDATE = ACTION_PREFIX + "UPDATE";
  private static final String EXTRA_URL = "URL";

  private Context context;
  private BroadcastReceiver receiver;

  public SubscriptionsManager(final Context context)
  {
    this.context = context;
    Timber.v("Initializing subscription management");

    receiver = new BroadcastReceiver()
    {
      @Override
      public void onReceive(final Context context, final Intent intent)
      {
        Timber.v("Received intent %s", intent);

        AdblockHelper.get().getProvider().waitForReady();
        if (intent.getAction().equals(ACTION_LIST))
        {
          list();
        }
        else
        {
          final String url = intent.getStringExtra(EXTRA_URL);
          Timber.d("Subscription = %s", url);
          final AdblockEngine adblockEngine = AdblockHelper.get().getProvider().getEngine();
          final FilterEngine filterEngine = adblockEngine.getFilterEngine();
          final Subscription subscription = adblockEngine.getFilterEngine().getSubscription(url);

          if (intent.getAction().equals(ACTION_ADD))
          {
            add(filterEngine, subscription);
          }
          else if (intent.getAction().equals(ACTION_REMOVE))
          {
            remove(filterEngine, subscription);
          }
          else if (intent.getAction().equals(ACTION_ENABLE))
          {
            enable(filterEngine, subscription);
          }
          else if (intent.getAction().equals(ACTION_DISABLE))
          {
            disable(filterEngine, subscription);
          }
          else if (intent.getAction().equals(ACTION_UPDATE))
          {
            update(filterEngine, subscription);
          }
        }
      }

      private void remove(final FilterEngine filterEngine, final Subscription subscription)
      {
        if (!assertIsListed(filterEngine, subscription))
        {
          return;
        }

        filterEngine.removeSubscription(subscription);
        Timber.d("Removed subscription");
      }

      private void update(final FilterEngine filterEngine, final Subscription subscription)
      {
        if (!assertIsListed(filterEngine, subscription))
        {
          return;
        }

        if (!assertIsEnabled(subscription))
        {
          return;
        }

        subscription.updateFilters();
        Timber.d("Forced subscription update");
      }

      private void enable(final FilterEngine filterEngine, final Subscription subscription)
      {
        if (!assertIsListed(filterEngine, subscription))
        {
          return;
        }

        if (!subscription.isDisabled())
        {
          Timber.e("Subscription is already enabled");
          return;
        }

        subscription.setDisabled(false);
        Timber.d("Enabled subscription");
      }

      private void disable(final FilterEngine filterEngine, final Subscription subscription)
      {
        if (!assertIsListed(filterEngine, subscription))
        {
          return;
        }

        if (!assertIsEnabled(subscription))
        {
          return;
        }

        subscription.setDisabled(true);
        Timber.d("Disabled subscription");
      }

      private void add(final FilterEngine filterEngine, final Subscription subscription)
      {
        if (filterEngine.getListedSubscriptions().contains(subscription))
        {
          if (subscription.isDisabled())
          {
            subscription.setDisabled(false);
            Timber.d("Enabled subscription");
          }
          else
          {
            Timber.e("Already listed and enabled subscription");
          }
        }
        else
        {
          filterEngine.addSubscription(subscription);
          Timber.d("Added subscription");
        }
      }

      private void list()
      {
        final AdblockEngine adblockEngine =
          AdblockHelper.get().getProvider().getEngine();
        final List<Subscription> subscriptions = adblockEngine.getFilterEngine().getListedSubscriptions();
        for (final Subscription subscription : subscriptions)
        {
          Timber.d("%s is %s",
            subscription.toString(), (subscription.isDisabled() ? "disabled" : "enabled"));
        }
      }

      private boolean assertIsListed(final FilterEngine filterEngine,
                                     final Subscription subscription)
      {
        if (!filterEngine.getListedSubscriptions().contains(subscription))
        {
          Timber.e("Subscription is not listed");
          return false;
        }
        return true;
      }

      private boolean assertIsEnabled(final Subscription subscription)
      {
        if (subscription.isDisabled())
        {
          Timber.e("Subscription is disabled");
          return false;
        }
        return true;
      }
    };

    final IntentFilter filter = new IntentFilter();
    filter.addAction(ACTION_LIST);
    filter.addAction(ACTION_ADD);
    filter.addAction(ACTION_REMOVE);
    filter.addAction(ACTION_ENABLE);
    filter.addAction(ACTION_DISABLE);
    filter.addAction(ACTION_UPDATE);
    context.registerReceiver(receiver, filter);
  }

  public void dispose()
  {
    context.unregisterReceiver(receiver);
    context = null;
  }
}