package org.infinispan.factories;


import org.infinispan.batch.BatchContainer;
import org.infinispan.commands.CommandsFactory;
import org.infinispan.commands.CommandsFactoryImpl;
import org.infinispan.commons.CacheConfigurationException;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.context.InvocationContextContainer;
import org.infinispan.context.NonTransactionalInvocationContextContainer;
import org.infinispan.context.TransactionalInvocationContextContainer;
import org.infinispan.distribution.L1Manager;
import org.infinispan.distribution.L1ManagerImpl;
import org.infinispan.eviction.ActivationManager;
import org.infinispan.eviction.ActivationManagerImpl;
import org.infinispan.eviction.EvictionManager;
import org.infinispan.eviction.EvictionManagerImpl;
import org.infinispan.eviction.PassivationManager;
import org.infinispan.eviction.PassivationManagerImpl;
import org.infinispan.factories.annotations.DefaultFactoryFor;
import org.infinispan.interceptors.locking.ClusteringDependentLogic;
import org.infinispan.loaders.manager.CacheLoaderManager;
import org.infinispan.loaders.manager.CacheLoaderManagerImpl;
import org.infinispan.notifications.cachelistener.CacheNotifier;
import org.infinispan.notifications.cachelistener.CacheNotifierImpl;
import org.infinispan.statetransfer.StateTransferLock;
import org.infinispan.statetransfer.StateTransferLockImpl;
import org.infinispan.transaction.TransactionCoordinator;
import org.infinispan.transaction.totalorder.TotalOrderManager;
import org.infinispan.transaction.xa.TransactionFactory;
import org.infinispan.transaction.xa.recovery.RecoveryAdminOperations;
import org.infinispan.util.concurrent.locks.containers.LockContainer;
import org.infinispan.util.concurrent.locks.containers.OwnableReentrantPerEntryLockContainer;
import org.infinispan.util.concurrent.locks.containers.OwnableReentrantStripedLockContainer;
import org.infinispan.util.concurrent.locks.containers.ReentrantPerEntryLockContainer;
import org.infinispan.util.concurrent.locks.containers.ReentrantStripedLockContainer;
import org.infinispan.xsite.BackupSender;
import org.infinispan.xsite.BackupSenderImpl;

import static org.infinispan.commons.util.Util.getInstance;

/**
 * Simple factory that just uses reflection and an empty constructor of the component type.
 *
 * @author Manik Surtani (<a href="mailto:manik@jboss.org">manik@jboss.org</a>)
 * @author Pedro Ruivo
 * @since 4.0
 */
@DefaultFactoryFor(classes = {CacheNotifier.class, CommandsFactory.class,
                              CacheLoaderManager.class, InvocationContextContainer.class,
                              PassivationManager.class, ActivationManager.class,
                              BatchContainer.class, EvictionManager.class,
                              TransactionCoordinator.class, RecoveryAdminOperations.class, StateTransferLock.class,
                              ClusteringDependentLogic.class, LockContainer.class,
                              L1Manager.class, TransactionFactory.class, BackupSender.class,
                              TotalOrderManager.class})
public class EmptyConstructorNamedCacheFactory extends AbstractNamedCacheComponentFactory implements AutoInstantiableFactory {

   @Override
   @SuppressWarnings("unchecked")
   public <T> T construct(Class<T> componentType) {
      Class<?> componentImpl;
      if (componentType.equals(ClusteringDependentLogic.class)) {
         CacheMode cacheMode = configuration.clustering().cacheMode();
         if (!cacheMode.isClustered()) {
            return componentType.cast(new ClusteringDependentLogic.LocalLogic());
         } else if (cacheMode.isInvalidation()) {
            return componentType.cast(new ClusteringDependentLogic.InvalidationLogic());
         } else if (cacheMode.isReplicated()) {
            return componentType.cast(new ClusteringDependentLogic.ReplicationLogic());
         } else {
            return componentType.cast(new ClusteringDependentLogic.DistributionLogic());
         }
      } else {
         boolean isTransactional = configuration.transaction().transactionMode().isTransactional();
         if (componentType.equals(InvocationContextContainer.class)) {
            componentImpl = isTransactional ? TransactionalInvocationContextContainer.class
                  : NonTransactionalInvocationContextContainer.class;
            return componentType.cast(getInstance(componentImpl));
         } else if (componentType.equals(CacheNotifier.class)) {
            return (T) new CacheNotifierImpl();
         } else if (componentType.equals(CommandsFactory.class)) {
            return (T) new CommandsFactoryImpl();
         } else if (componentType.equals(CacheLoaderManager.class)) {
            return (T) new CacheLoaderManagerImpl();
         } else if (componentType.equals(PassivationManager.class)) {
            return (T) new PassivationManagerImpl();
         } else if (componentType.equals(ActivationManager.class)) {
            return (T) new ActivationManagerImpl();
         } else if (componentType.equals(BatchContainer.class)) {
            return (T) new BatchContainer();
         } else if (componentType.equals(TransactionCoordinator.class)) {
            return (T) new TransactionCoordinator();
         } else if (componentType.equals(RecoveryAdminOperations.class)) {
            return (T) new RecoveryAdminOperations();
         } else if (componentType.equals(StateTransferLock.class)) {
            return (T) new StateTransferLockImpl();
         } else if (componentType.equals(EvictionManager.class)) {
            return (T) new EvictionManagerImpl();
         } else if (componentType.equals(LockContainer.class)) {
            boolean  notTransactional = !isTransactional;
            LockContainer<?> lockContainer = configuration.locking().useLockStriping() ?
                  notTransactional ? new ReentrantStripedLockContainer(configuration.locking().concurrencyLevel())
                        : new OwnableReentrantStripedLockContainer(configuration.locking().concurrencyLevel()) :
                  notTransactional ? new ReentrantPerEntryLockContainer(configuration.locking().concurrencyLevel())
                        : new OwnableReentrantPerEntryLockContainer(configuration.locking().concurrencyLevel());
            return (T) lockContainer;
         } else if (componentType.equals(L1Manager.class)) {
            return (T) new L1ManagerImpl();
         } else if (componentType.equals(TransactionFactory.class)) {
            return (T) new TransactionFactory();
         } else if (componentType.equals(BackupSender.class)) {
            return (T) new BackupSenderImpl(globalConfiguration.sites().localSite());
         } else if (componentType.equals(TotalOrderManager.class)) {
            return (T) new TotalOrderManager();
         }
      }

      throw new CacheConfigurationException("Don't know how to create a " + componentType.getName());

   }
}
