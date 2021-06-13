package rpc

import kotlinx.serialization.Serializable


@Serializable
class ApiRequest1(val ping: String) : Request<ApiResponse1>

@Serializable
class ApiResponse1(val pong: String)

@Serializable
class ApiRequestSum(val a: Int, val b: Int) : Request<ApiResponseSum>

@Serializable
class ApiResponseSum(val sum: Int)

@Serializable
class ApiRequestMul(val a: Int, val b: Int) : Request<ApiResponseMul>

@Serializable
class ApiResponseMul(val mul: Int)

