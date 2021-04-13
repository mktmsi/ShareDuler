<?php
ini_set( 'display_errors', 1 );

include 'DBHelper.php';

$db =  new DBHelper();

$schedule = $_GET['sche'];
$dateMillis = $_GET['date'];
$placeName = $_GET['place'];
$latitude = $_GET['lat'];
$latitude = str_replace("_", ".", $latitude);
$longitude = $_GET['lon'];
$longitude = str_replace("_", ".", $longitude);
$name = $_GET['name'];
$url = uniqid(rand());

$db->insert_event($schedule,$dateMillis, $placeName, $latitude, $longitude, $url, $name);

$jsonArray = array(
  'url' => $url
);

header("Content-Type: application/json; charset=UTF-8");
echo json_encode($jsonArray, JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_QUOT | JSON_HEX_AMP);

?>
