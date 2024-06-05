import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.{AmazonClientException, AmazonServiceException}
import com.typesafe.config.ConfigFactory

import java.io.File

object main {
  val config = ConfigFactory.load()
  private val awsAccessKeyId = config.getString("aws.access-key-id")
  private val awsSecretAccessKey = config.getString("aws.secret-access-key")

  val BUCKET_NAME = "mytestbucket-payoda" //  bucket-name
  val FILE_PATH = "/Users/aniketsharma/Downloads/IMG_1104.jpg" //  path
  val FILE_NAME = "keyName" //  key-name

  implicit val system = ActorSystem(Behaviors.empty, "MyActorSystem")

  def main(args: Array[String]): Unit = {
    val awsCredentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)
    val amazonS3Client = new AmazonS3Client(awsCredentials)
    val route =
      post {
        path("save-file") {
          // create a new bucket - already created in s3
          try {
            val file = new File(FILE_PATH)
            amazonS3Client.putObject(BUCKET_NAME, FILE_NAME, file)
          }
          catch {
            case ase: AmazonServiceException => System.err.println("Exception: " + ase.toString)
            case ace: AmazonClientException => System.err.println("Exception: " + ace.toString)
          }
          complete(StatusCodes.OK, "File Uploaded to S3")
        }
      }
    Http().newServerAt("0.0.0.0", 6080).bind(route)
    println("Server online at http://0.0.0.0:6080/")
  }
}