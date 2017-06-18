def ruleParser(implicit context: ParsingContext): Parser[Rule] = {
  def matchesParserRec(
      context: ParsingContext,
      accMatches: List[Match]): Parser[List[Match]] = {
    val matchParsers = context.matchExtensions.map(_.matchParsers).flatten

    for {
      newMatch <- optional(oneOf(matchParsers: _*))
      accNext <- newMatch match {
        case Some(m) => matchesParserRec(
          context.addMatchExtensions(m.extensionsEnabled), accMatches :+ m)
        case None => pure(accMatches)
      }
    } yield accNext
  }

  for {
    matches <- matchesParserRec(context, Nil)
    target  <- oneOf(context.targetExtensions.map(_.targetParser): _*)
  } yield Rule(matches, target)
}
