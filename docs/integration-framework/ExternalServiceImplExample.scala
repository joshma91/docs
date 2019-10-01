package io.openlaw.services

import integration.framework.openlaw.{Empty, EthereumAddressResponse, ExecuteRequest, ExecuteResponse, MarkupInterfaceResponse}
import integration.framework.openlaw.ExternalServiceGrpc.ExternalService
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

/**
  * ExternalServiceImpl extends the ExternalService trait generated by the Protoc Compiler. This is the server implementation
  * that responds to gRPC requests from OpenLaw Integration Framework.
  *
  * @param priceConverterService - The service implementation which hits the Coin Market Cap API to get the exchange rates.
  * @param configService - The configuration service which provide access to server properties and environment variables.
  * @param identityService - The service which provides the Ethereum account of to get the Public Ethereum Address as service identity.
  * @param ec - Scala execution context.
  */
@Singleton
class ExternalServiceImpl @Inject()(priceConverterService: PriceConverterService,
                                    configService: ConfigService,
                                    identityService: IdentityService)(implicit ec: ExecutionContext)
  extends ExternalService {

  /**
    * Gets the Ethereum Public Address from the service which is used to verify events sent from the service to OpenLaw VM.
    */
  override def getEthereumAddress(request: Empty): Future[EthereumAddressResponse] =
    Future.successful(EthereumAddressResponse().withAddress(identityService.getAccount.getAddress.withLeading0x))


  /**
    * Gets the server Markup Interface definition which is used in a OpenLaw Agreement with ExternalCall or ExternalSignature variable types.
    * The expected Markup Interface definition must follow the standard:
    *  - [[Input:Structure(inputField1: <Type>; inputField2: <Type>; inputFieldN: <Type>)]] [[Output:Structure(outputField1: <Type>; outputField2: <Type>; outputFieldN: <Type>)]]
    *  - <Type> - can be replaced by: Text, Number and Date.
    * A basic Markup Interface for the Coin Market Cap service can be defined as a String of value:
    *  - "[[Input:Structure(fromCurrency: Text; toCurrency: Text; amount: Number)]] [[Output:Structure(currency: Text; price: Number; lastUpdate: Text)]]"
    * The standard Markup Interface for any e-Signature service is defined by the following String value:
    *  - "[[Input:Structure(signerEmail: Text; contractContentBase64: Text; contractTitle: Text)]] [[Output:Structure(signerEmail: Text; signature: Text; recordLink: Text)]]"
    * Any e-Signature service must use the exact same Markup Interface as described above, otherwise the e-Signature will not be validated by the OpenLaw VM.
    */
  override def getMarkupInterface(request: Empty): Future[MarkupInterfaceResponse] =
    Future.successful(MarkupInterfaceResponse().withDefinition(configService.getMarkupInterface))

  /**
    * Executes the request from OpenLaw Integration Framework and waits for the External Service response.
    */
  override def execute(request: ExecuteRequest): Future[ExecuteResponse] =
    priceConverterService.convert(request)

}
