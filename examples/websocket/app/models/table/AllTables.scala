/* Generated File */
package models.table

import com.kyleu.projectile.services.database.slick.SlickQueryService.imports._

object AllTables {
  val schema = Seq(
    models.table.auth.Oauth2InfoRowTable.query.schema,
    models.table.auth.PasswordInfoRowTable.query.schema,
    models.table.auth.SystemUserRowTable.query.schema,
    models.table.note.NoteRowTable.query.schema
  )
}
