package com.stackmob.customcode.dev.test

import org.specs2.Specification
import scala.reflect.ClassTag
import org.specs2.matcher.{Expectable, Matcher, MatchResult}
import com.stackmob.sdkapi.http.response.HttpResponse
import com.stackmob.newman.response.{HttpResponse => NewmanHttpResponse}

trait CustomMatchers { this: Specification =>
  protected def beThrowable(expected: Throwable) = beLeft[Throwable].like {
    case t: Throwable => t must beEqualTo(expected)
  }

  protected def beThrowableInstance[T <: Throwable: ClassTag] = beLeft[Throwable].like {
    case t => t must beAnInstanceOf[T]
  }

  protected def beThrowableInstance[T <: Throwable: ClassTag, U](fn: Throwable => MatchResult[U]) = beLeft[Throwable].like {
    case t => {
      val instance = t must beAnInstanceOf[T]
      val res = fn(t)
      instance and res
    }
  }

  /**
   * a matcher for HttpResponse
   * @param expectedCode the code that's expected
   * @param bodyContains a substring that's expected in the body of the HttpResponse
   */
  protected class HttpResponseMatcher(expectedCode: Int, bodyContains: String) extends Matcher[HttpResponse] {
    override def apply[S <: HttpResponse](s: Expectable[S]) = {
      val resp = s.value
      val codeRes = resp.getCode must beEqualTo(expectedCode)
      val bodyRes = resp.getBody must contain(bodyContains)
      result(codeRes and bodyRes,
        s"${s.description} matches code $expectedCode and contains $bodyContains in the body",
        s"${s.description} does not match code $expectedCode and contains $bodyContains in the body",
        s
      )
    }
  }

  /**
   * create a matcher for HttpResponse
   * @param code the expected HttpResponse code
   * @param bodyContains a string that must be in the body of the HttpResponse
   * @return the new HttpResponse matcher
   */
  protected def beResponse(code: Int, bodyContains: String): Matcher[HttpResponse] = {
    new HttpResponseMatcher(code, bodyContains)
  }

  protected def beResponse(resp: NewmanHttpResponse): Matcher[HttpResponse] = {
    new HttpResponseMatcher(resp.code.code, resp.bodyString)
  }
}
