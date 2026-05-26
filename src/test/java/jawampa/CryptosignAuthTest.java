package jawampa;

import jawampa.auth.client.Cryptosign;
import jawampa.connection.IWampConnectorProvider;
import jawampa.transport.netty.NettyWampClientConnectorProvider;
import jawampa.transport.netty.NettyWampConnectionConfig;
import rx.functions.Action0;
import rx.functions.Action1;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CryptosignAuthTest {

   public static void main(String[] args) {
      new CryptosignAuthTest().start();
   }

   public void start() {

      IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
      WampClientBuilder builder = new WampClientBuilder();

      final WampClient client1;
      try {

         builder.withConnectorProvider(connectorProvider)
               .withUri("wss://lj-test-v2.wamp.strix.com.ar/ws")
               .withRealm("com.magenta.test")
               // .withUri("ws://127.0.0.1:18080/ws")
               // .withRealm("com.leapsight.test")
               .withAuthId("cryptosign_user")
               // .withAuthId("device1")
               .withAuthMethod(new Cryptosign(
                     "4ffddd896a530ce5ee8c86b83b0d31835490a97a9cd718cb2f09c9fd31c4a7d71766c9e6ec7d7b354fd7a2e4542753a23cae0b901228305621e5b8713299ccdd",
                     "1766c9e6ec7d7b354fd7a2e4542753a23cae0b901228305621e5b8713299ccdd"))
               .withInfiniteReconnects()
               .withReconnectInterval(5, TimeUnit.SECONDS)
               .withConnectionConfiguration((new NettyWampConnectionConfig.Builder()).build());

         client1 = builder.build();
         client1.open();
      } catch (Exception e) {
         System.err.println("Error building wamp client: " + e.getLocalizedMessage());
         return;
      }

      client1.statusChanged().subscribe(new Action1<WampClient.State>() {
         @Override
         public void call(WampClient.State t1) {
            System.out.println("Session1 status changed to " + t1);
            if (t1 instanceof WampClient.DisconnectedState) {
               System.out.println(
                     "Session1 Disconnected reason: " + ((WampClient.DisconnectedState) t1).disconnectReason());
            }
         }
      }, new Action1<Throwable>() {
         @Override
         public void call(Throwable t) {
            System.out.println("Session1 ended with error " + t);
         }
      }, new Action0() {
         @Override
         public void call() {
            System.out.println("Session1 ended normally");
         }
      });

      waitUntilKeypressed();
      System.out.println("Closing the client 1");
      client1.close().toBlocking().last();
   }

   private void waitUntilKeypressed() {
      try {
         System.in.read();
         while (System.in.available() > 0) {
            System.in.read();
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
