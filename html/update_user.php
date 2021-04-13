<?php
ini_set( 'display_errors', 1 );

include 'DBHelper.php';

$db =  new DBHelper();

$id = $_GET['id'];
$name = $_GET['name'];

$db->update_user($id, $name);

$jsonArray = array(
  'id' => 'id'
);
header("Content-Type: application/json; charset=UTF-8");
echo json_encode($jsonArray, JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_QUOT | JSON_HEX_AMP);
?>
