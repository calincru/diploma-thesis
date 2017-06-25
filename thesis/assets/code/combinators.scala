def optional[T](p: Parser[T]): Parser[Option[T]] =
  (p >>= (x => pure(Option(x)))) <<|> pure(None)

def many[T](p: Parser[T]): Parser[List[T]] =
  some(p) <<|> pure(Nil)

def some[T](p: Parser[T]): Parser[List[T]] =
  for {
    x <- p
    y <- many(p)
  } yield (x +: y)
