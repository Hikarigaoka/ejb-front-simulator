package com.example.ejbapi.ejb;

import com.example.ejbapi.dto.ApiRequestDto;
import com.example.ejbapi.dto.ApiResponseDto;

/**
 * EJBリモートビジネスインターフェース
 *
 * <p>実際のEJBコンテナ(JBoss / WildFly等)にデプロイされた
 * EJBのリモートインターフェースに相当する。
 *
 * <h3>実EJB接続への切り替え手順</h3>
 * <ol>
 *   <li>EJBサーバー側の @Remote インターフェースを本インターフェースに合わせる</li>
 *   <li>EjbServiceConfig を有効化して JNDI ルックアップ実装に切り替える</li>
 *   <li>EjbServiceMockImpl の @Service を削除またはプロファイルで除外する</li>
 * </ol>
 *
 * <pre>
 * // JBoss/WildFly JNDI ルックアップ例:
 * Properties props = new Properties();
 * props.put(Context.INITIAL_CONTEXT_FACTORY,
 *           "org.wildfly.naming.client.WildFlyInitialContextFactory");
 * props.put(Context.PROVIDER_URL, "http-remoting://ejb-server:8080");
 * props.put(Context.SECURITY_PRINCIPAL, "appuser");
 * props.put(Context.SECURITY_CREDENTIALS, "secret");
 *
 * InitialContext ctx = new InitialContext(props);
 * EjbServiceInterface ejb = (EjbServiceInterface) ctx.lookup(
 *     "ejb:/app-name/module-name/EjbServiceBean!" +
 *     "com.example.ejbapi.ejb.EjbServiceInterface"
 * );
 * </pre>
 */
public interface EjbServiceInterface {

    /**
     * EJBビジネスロジックを実行する。
     *
     * @param request DTO化されたリクエスト
     * @return EJBからの処理結果DTO
     */
    ApiResponseDto process(ApiRequestDto request);
}
